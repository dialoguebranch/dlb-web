/*
 *
 *                   Copyright (c) 2023 Fruit Tree Labs (www.fruittreelabs.com)
 *
 *     This material is part of the DialogueBranch Platform, and is covered by the MIT License
 *      as outlined below. Based on original source code licensed under the following terms:
 *
 *                                            ----------
 *
 * Copyright 2019-2022 WOOL Foundation - Licensed under the MIT License:
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dialoguebranch.web.service.execution;

import com.dialoguebranch.exception.DLBException;
import com.dialoguebranch.i18n.DLBTranslationContext;
import com.dialoguebranch.model.DLBDialogue;
import com.dialoguebranch.model.DLBFileDescription;
import com.dialoguebranch.parser.DLBFileLoader;
import com.dialoguebranch.parser.DLBProjectParser;
import com.dialoguebranch.parser.DLBProjectParserResult;
import com.dialoguebranch.web.service.controller.schema.LoginParametersPayload;
import com.dialoguebranch.web.service.controller.schema.LoginResultPayload;
import com.dialoguebranch.web.service.exception.DLBServiceConfigurationException;
import com.dialoguebranch.web.service.Configuration;
import com.dialoguebranch.web.service.UserCredentials;
import com.dialoguebranch.web.service.UserFile;
import com.dialoguebranch.web.service.storage.AzureDataLakeStore;
import com.dialoguebranch.model.DLBProject;
import nl.rrd.utils.AppComponents;
import nl.rrd.utils.exception.DatabaseException;
import nl.rrd.utils.exception.ParseException;
import org.slf4j.Logger;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The DialogueBranch Web Service maintains one instance of a {@link ApplicationManager}. This class keeps
 * track of the different active {@link UserService} instances that are needed to serve individual
 * user's of the DialogueBranch Web Service, as well as other application wide objects.
 *
 * @author Harm op den Akker
 * @author Tessa Beinema
 */
public class ApplicationManager {

	private final Logger logger = AppComponents.getLogger(getClass().getSimpleName());
	private final DLBProject dlbProject;
	private final List<UserService> activeUserServices = new ArrayList<>();
	private final List<UserCredentials> userCredentials;
	private String externalVariableServiceAPIToken;
	private AzureDataLakeStore azureDataLakeStore = null;

	// ---------------------------------- //
	// ---------- Constructors ---------- //
	// ---------------------------------- //
	
	/**
	 * Creates an instance of an {@link ApplicationManager}, that loads in a predefined list of DialogueBranch
	 * dialogues.
	 *
	 * @throws DLBServiceConfigurationException In case any part of the application could not be
	 *                                   initialized due to an incorrectly set config parameter.
	 */
	public ApplicationManager(DLBFileLoader dlbFileLoader) throws DLBServiceConfigurationException {
		UserServiceFactory appConfig = UserServiceFactory.getInstance();
		DLBProjectParser dlbProjectParser = new DLBProjectParser(dlbFileLoader);
		DLBProjectParserResult readResult;
		try {
			readResult = dlbProjectParser.parse();
		} catch (IOException ex) {
			throw new RuntimeException("Error while reading DialogueBranch project: " + ex.getMessage(), ex);
		}
		for (String path : readResult.getParseErrors().keySet()) {
			logger.error("Failed to parse " + path + ":");
			for (ParseException ex : readResult.getParseErrors().get(path)) {
				logger.error("*** " + ex.getMessage());
			}
		}
		for (String path : readResult.getWarnings().keySet()) {
			logger.warn("Warning at parsing " + path + ":");
			for (String warning : readResult.getWarnings().get(path)) {
				logger.warn("*** " + warning);
			}
		}
		if (!readResult.getParseErrors().isEmpty())
			throw new RuntimeException("Failed to load all dialogues.");
		dlbProject = readResult.getProject();
		appConfig.setDialogueBranchProject(dlbProject);

		// Read all UserCredentials from users.xml
		try {
			userCredentials = UserFile.read();
		} catch (ParseException | IOException e) {
			throw new RuntimeException(e);
		}

		// login to external variable service
		Configuration config = AppComponents.get(Configuration.class);
		if(config.getExternalVariableServiceEnabled()) {
			try {
				this.loginToExternalVariableService();
			} catch (Exception e) {
				logger.info(e.toString());
				throw new RuntimeException(e);
			}
		}

		if(Configuration.getInstance().getAzureDataLakeEnabled()) {
			try {
				azureDataLakeStore = new AzureDataLakeStore();
			} catch(DLBServiceConfigurationException e) {
				logger.error("Error configuring Azure Data Lake: " + e.getMessage());
				throw e;
			}
		}
	}
	
	// ---------- Getters:
	
	public List<DLBFileDescription> getDialogueDescriptions() {
		return new ArrayList<>(dlbProject.getDialogues().keySet());
	}

	/**
	 * Returns the list of {@link UserCredentials} available for this {@link ApplicationManager}.
	 * @return the list of {@link UserCredentials} available for this {@link ApplicationManager}.
	 */
	public List<UserCredentials> getUserCredentials() {
		return userCredentials;
	}

	/**
	 * Returns the {@link UserCredentials} object associated with the given {@code username}, or
	 * {@code null} if no such user is known.
	 * @param username the username of the user to look for.
	 * @return the {@link UserCredentials} object or {@code null}.
	 */
	public UserCredentials getUserCredentialsForUsername(String username) {
		for(UserCredentials uc : userCredentials) {
			if(uc.getUsername().equals(username)) return uc;
		}
		return null;
	}

	public AzureDataLakeStore getAzureDataLakeStore() {
		return azureDataLakeStore;
	}
	
	// ---------- Service Management:
	
	/**
	 * Returns an active {@link UserService} object for the given {@code userId}. Retrieves from an
	 * internal list of active {@link UserService}s, or instantiates a new {@link UserService} if no
	 * {@link UserService} is active for the given user.
	 * @param userId the identifier of the user that is interacting with the {@link UserService}.
	 * @return a {@link UserService} object that can handle the communication with the user.
	 */
	public UserService getActiveUserService(String userId) throws DatabaseException {
		
		for(UserService userService : activeUserServices) {
			if(userService.getDialogueBranchUser().getId().equals(userId)) {
				return userService;
			}
		}
		
		logger.info("No active UserService for userId '" + userId
				+ "' creating UserService instance.");
		
		// Initialize new userService
		UserServiceFactory userServiceFactory = UserServiceFactory.getInstance();
		UserService newUserService;
		try {
			newUserService = userServiceFactory.createUserService(userId, this);
		} catch (IOException ex) {
			String error = "Can't create userService: " + ex.getMessage();
			logger.error(error, ex);
			throw new RuntimeException(error, ex);
		}
		
		// Store as "active userService"
		activeUserServices.add(newUserService);
		return newUserService;
	}
	
	/**
	 * Removes the given {@link UserService} from the set of active {@link UserService}s in
	 * this {@link ApplicationManager}.
	 * @param userService the {@link UserService} to remove.
	 * @return {@code true} if the given was successfully removed, {@code false} if
	 * it was not present on the list of active {@link UserService}s in the first place.
	 */
	public boolean removeUserService(UserService userService) {
		return activeUserServices.remove(userService);
	}
	
	// ---------- Dialogue Management:

	public DLBDialogue getDialogueDefinition(DLBFileDescription dialogueDescription,
											 DLBTranslationContext translationContext)
			throws DLBException {
		DLBDialogue dialogue;
		if (translationContext == null)
			dialogue = dlbProject.getDialogues().get(dialogueDescription);
		else
			dialogue = dlbProject.getTranslatedDialogue(dialogueDescription, translationContext);
		if (dialogue != null)
			return dialogue;
		throw new DLBException(DLBException.Type.DIALOGUE_NOT_FOUND,
			"Pre-loaded dialogue not found for dialogue '" +
					dialogueDescription.getDialogueName() + "' in language '" +
					dialogueDescription.getLanguage() + "'.");
	}
	
	public List<DLBFileDescription> getAvailableDialogues() {
		return new ArrayList<>(dlbProject.getDialogues().keySet());
	}

	public String getExternalVariableServiceAPIToken() {
		return externalVariableServiceAPIToken;
	}

	public void setExternVariableServiceAPIToken(String externalVariableServiceAPIToken) {
		this.externalVariableServiceAPIToken = externalVariableServiceAPIToken;
	}

	public void loginToExternalVariableService() {
		Configuration config = AppComponents.get(Configuration.class);

		String loginUrl = config.getExternalVariableServiceURL()
				+ "/v" + config.getExternalVariableServiceAPIVersion()
				+ "/auth/login";

		logger.info("Attempting login to external variable service at " + loginUrl);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.valueOf("application/json"));

		RestTemplate restTemplate = new RestTemplate();

		LoginParametersPayload loginParametersPayload = new LoginParametersPayload();
		loginParametersPayload.setUser(config.getExternalVariableServiceUsername());
		loginParametersPayload.setPassword(config.getExternalVariableServicePassword());
		loginParametersPayload.setTokenExpiration(null);
		HttpEntity<LoginParametersPayload> request =
				new HttpEntity<>(loginParametersPayload, headers);

		ResponseEntity<LoginResultPayload> response = restTemplate.postForEntity(
				loginUrl, request, LoginResultPayload.class);

		if (response.getStatusCode() == HttpStatus.OK) {
			LoginResultPayload loginResultPayload = response.getBody();
			if(loginResultPayload != null) {
				this.setExternVariableServiceAPIToken(loginResultPayload.getToken());
				logger.info("User '"+config.getExternalVariableServiceUsername()+"' " +
						"logged in successfully to external variable service.");
			} else {
				logger.error("Login to External Variable Service failed with status code: "
						+ response.getStatusCode());
			}
		} else {
			logger.info("Login failed: " + response.getStatusCode());
		}
	}
}

/*
 *
 *                Copyright (c) 2023-2024 Fruit Tree Labs (www.fruittreelabs.com)
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

package com.dialoguebranch.web.service.storage;

import com.dialoguebranch.web.service.Configuration;
import com.dialoguebranch.web.service.execution.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.rrd.utils.AppComponents;
import nl.rrd.utils.exception.DatabaseException;
import nl.rrd.utils.io.FileUtils;
import nl.rrd.utils.json.JsonMapper;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * A {@link LoggedDialogueStore} is a class that acts as the storage for {@link ServerLoggedDialogue}
 * objects, used in the execution of dialogues by the DialogueBranch Web Service.
 *
 * <p>A {@link LoggedDialogueStore} does not maintain any data in-memory, but immediately stores
 * any changes made to the configured storage mechanism.</p>
 *
 * @author Harm op den Akker
 */
public class LoggedDialogueStore {

	private final Logger logger = AppComponents.getLogger(getClass().getSimpleName());
	private final Configuration config = Configuration.getInstance();
	private final UserService userService;
	private final String userId;
	private final File userLogDirectory;
	private static final Object LOCK = new Object();
	private ServerLoggedDialogue latestStoredServerLoggedDialogue = null;

	// ------------------------------------ //
	// ---------- Constructor(s) ---------- //
	// ------------------------------------ //

	/**
	 * Creates an instance of a {@link LoggedDialogueStore} for the user identified by the given
	 * {@code userId}, with a reference to that user's {@link UserService}. Upon instantiation,
	 * this {@link LoggedDialogueStore} attempts to create the directory to be used for logging
	 * dialogues (as defined in the {@link Configuration}).
	 * @param userId the identifier of the DialogueBranch User for which to instantiate this {@link
	 *                   LoggedDialogueStore}
	 * @param userService the {@link UserService} associated with this LoggedDialogueStore
	 * @throws IOException in case of an error instantiating the log folder.
	 */
	public LoggedDialogueStore(String userId, UserService userService) throws IOException {
		logger.info("Initializing LoggedDialogueStore for user '" + userId + "'.");

		this.userService = userService;
		this.userId = userId;

		File dialogueLogDirectory = new File(config.getDataDir() + File.separator
				+ config.getDirectoryNameDialogues());

		// If the application's dialogue log directory doesn't exist yet
		if(!dialogueLogDirectory.exists()) {

			// Create this directory, and if it fails throw an error
			if(!dialogueLogDirectory.mkdirs()) {
				throw new IOException("Unable to create the dialogue log folder at "
						+ dialogueLogDirectory.getAbsolutePath());
			} else {
				logger.info("Created dialogue log directory at: "+dialogueLogDirectory);
			}
		}

		// Now instantiate this user's specific directory
		this.userLogDirectory = new File(dialogueLogDirectory, userId);

		// If the folder doesn't exist, initialize it
		if(!userLogDirectory.exists()) {
			if(userLogDirectory.mkdirs()) {
				logger.info("Created user's dialogue log directory at: "+userLogDirectory);
				// The user  directory was created. In case an Azure Data Lake backup service is
				// enabled, check if there is data to populate this directory here.
				if(config.getAzureDataLakeEnabled()) {
					try {
						userService.getApplicationManager().getAzureDataLakeStore().
								populateLocalDialogueLogs(userId);
					} catch(IOException e) {
						logger.error("Error populating local dialogue log folder from Azure Data " +
							"Lake. It is possible that dialogue log information that is " +
							"available on the Azure Data Lake should have been synchronised to " +
							"the local DialogueBranch Web Service storage, but something went wrong in " +
							"doing so. This does not warrant interrupting the current request, " +
							"so operation has continued as if no log information was available.");
					}
				}
			} else {
				throw new IOException("Unable to create the user's log folder at: "
						+ userLogDirectory.getAbsolutePath());
			}
		}
	}

	// --------------------------------------- //
	// ---------- Public Operations ---------- //
	// --------------------------------------- //

	public ServerLoggedDialogue findLoggedDialogue(String id)
			throws DatabaseException, IOException {
		return readLatestDialogueWithConditions(false,null,id);
	}

	public ServerLoggedDialogue findLatestOngoingDialogue(String dialogueName)
			throws DatabaseException, IOException {
		return readLatestDialogueWithConditions(true,dialogueName,null);
	}

	public ServerLoggedDialogue findLatestOngoingDialogue()
			throws IOException, DatabaseException {
		return readLatestDialogueWithConditions(true, null, null);
	}

	public void setDialogueCancelled(ServerLoggedDialogue serverLoggedDialogue)
			throws DatabaseException, IOException {
		serverLoggedDialogue.setCancelled(true);
		saveToSession(serverLoggedDialogue);
	}

	public void saveToSession(ServerLoggedDialogue dialogue)
			throws DatabaseException, IOException {
		this.latestStoredServerLoggedDialogue = dialogue;
		synchronized(LOCK) {
			List<ServerLoggedDialogue> dialogues = readSessionWith(dialogue);
			saveToSession(dialogue.getSessionId(), dialogue.getSessionStartTime(), dialogues);
		}
	}

	/**
	 * Checks whether the given {@code sessionId} exists for this user.
	 * @param sessionId the sessionId for which to check.
	 * @return true if the sessionId exists, false otherwise.
	 */
	public boolean existsSessionId(String sessionId) throws DatabaseException {
		// First check whether the latest stored in-memory dialogue may be a match
		if(latestStoredServerLoggedDialogue != null) {
			if(latestStoredServerLoggedDialogue.getSessionId() != null) {
				if(latestStoredServerLoggedDialogue.getSessionId().equals(sessionId)) return true;
			}
		}

		// If not, check through the available files
		File[] userLogFiles;

		synchronized (LOCK) {
			userLogFiles = userLogDirectory.listFiles();
		}

		if(userLogFiles == null) throw new DatabaseException("Error retrieving file listing " +
				"from dialogue log directory for user '" + userId + "'.");

		for(File f : userLogFiles) {
			if (f.getName().endsWith(sessionId + ".json")) {
				return true;
			}
		}
		return false;
	}

	public List<ServerLoggedDialogue> readSession(String sessionId) throws DatabaseException, IOException {
		File[] userLogFiles;

		synchronized (LOCK) {
			userLogFiles = userLogDirectory.listFiles();
		}

		if(userLogFiles == null) throw new DatabaseException("Error retrieving file listing " +
				"from dialogue log directory for user '" + userId + "'.");

		for(File f : userLogFiles) {
			if (f.getName().endsWith(sessionId + ".json")) {
				return readSession(f);
			}
		}
		return new ArrayList<>();
	}

	// -------------------------------------------------- //
	// ---------- Private Read & Write Methods ---------- //
	// -------------------------------------------------- //

	private void saveToSession(String sessionId, long sessionStartTime,
									  List<ServerLoggedDialogue> dialogues) throws IOException {
		synchronized (LOCK) {
			String json = JsonMapper.generate(dialogues);
			File dataFile = new File(userLogDirectory, sessionStartTime + " " + sessionId +
					".json");
			FileUtils.writeFileString(dataFile, json);
			if(config.getAzureDataLakeEnabled()) {
				userService.getApplicationManager().getAzureDataLakeStore()
						.writeLoggedDialogueFile(userId,dataFile);
			}
		}
	}

	private List<ServerLoggedDialogue> readSession(String sessionId, long sessionStartTime)
			throws DatabaseException, IOException {
		File dataFile = new File(userLogDirectory, sessionStartTime + " " + sessionId +
				".json");
		return readSession(dataFile);
	}

	private List<ServerLoggedDialogue> readSession(File sessionFile)
			throws DatabaseException, IOException {
		List<ServerLoggedDialogue> result;
		synchronized (LOCK) {
			if (!sessionFile.exists())
				return new ArrayList<>();
			ObjectMapper mapper = new ObjectMapper();
			try {
				result = mapper.readValue(sessionFile,
						new TypeReference<>() {
						});
			} catch (JsonProcessingException ex) {
				throw new DatabaseException(
						"Failed to parse logged dialogues: " + sessionFile.getAbsolutePath() +
								": " + ex.getMessage(), ex);
			}
		}
		result.sort(Comparator.comparingLong(ServerLoggedDialogue::getUtcTime));
		return result;
	}

	/**
	 * Provide the complete list of all LoggedDialogues that are part of the same session as the
	 * given serverLoggedDialogue, including itself.
	 *
	 * @param serverLoggedDialogue the {@link ServerLoggedDialogue} for which to retrieve all of his friends.
	 * @return a List of ServerLoggedDialogue objects that form the complete session that the given
	 *         {@code serverLoggedDialogue} is part of, including itself
	 * @throws DatabaseException in case of an error reading from the dialogue log files.
	 * @throws IOException in case of an error reading from the dialogue log files.
	 */
	private List<ServerLoggedDialogue> readSessionWith(ServerLoggedDialogue serverLoggedDialogue)
			throws DatabaseException, IOException {
		// Read all logged dialogues in this session from file
		List<ServerLoggedDialogue> dialogues = readSession(serverLoggedDialogue.getSessionId(),
				serverLoggedDialogue.getSessionStartTime());

		// Remove any serverLoggedDialogue (well, it should only be 1) that has the same id as the one
		// we are adding.
		dialogues.removeIf(dialogue -> dialogue.getId().equals(serverLoggedDialogue.getId()));

		// Add the new (updated) serverLoggedDialogue provided
		dialogues.add(serverLoggedDialogue);

		// Sort by time
		dialogues.sort(Comparator.comparingLong(ServerLoggedDialogue::getUtcTime));
		return dialogues;
	}

	/**
	 * Dig through the given {@code user}'s log files, and look for the latest
	 * {@link ServerLoggedDialogue} that matches the conditions provided. This method will look through
	 * all the user's dialogue log files in order (newest to oldest), and return the first
	 * occurrence of a {@link ServerLoggedDialogue} that matches all conditions.
	 *
	 * <p>If {@code mustBeOngoing} is {@code true} this method will only return a
	 * {@link ServerLoggedDialogue} for which the #isCancelled and #isCompleted parameters are both false.
	 * Otherwise, these parameters are ignored.</p>
	 *
	 * <p>If a {@code dialogueName} is provided, the returned {@link ServerLoggedDialogue} must have this
	 * given dialogue name. If {@code null} is provided, the condition is ignored.</p>
	 *
	 * <p>If a {@code id} is provided, the returned {@link ServerLoggedDialogue} must have this id. If
	 * {@code null} is provided, the condition is ignored.</p>
	 *
	 * <p>Finally, if no {@link ServerLoggedDialogue} is found that matches all given conditions, this
	 * method will return {@code null}.</p>
	 *
	 * @param mustBeOngoing true if this method should only look for "ongoing" dialogues.
	 * @param dialogueName an optional dialogue name to look for (or {@code null}).
	 * @param id an optional id to look for (or {@code null}).
	 * @return the {@link ServerLoggedDialogue} that matches the conditions, or {@code null} if none can
	 *         be found.
	 * @throws DatabaseException in case of an error reading from the dialogue log files.
	 * @throws IOException in case of an error reading from the dialogue log files.
	 */
	private ServerLoggedDialogue readLatestDialogueWithConditions(boolean mustBeOngoing,
																  String dialogueName, String id)
			throws DatabaseException, IOException {

		// We maintain a reference to the latest stored ServerLoggedDialogue in memory, which
		// is the prime candidate for any search, so we check it first.
		if(this.latestStoredServerLoggedDialogue != null) {
			boolean match = true;

			if(mustBeOngoing) {
				if(latestStoredServerLoggedDialogue.isCancelled()
						|| latestStoredServerLoggedDialogue.isCompleted()) match = false;
			}

			if(match && dialogueName != null) {
				if(!latestStoredServerLoggedDialogue.getDialogueName().equals(dialogueName))
					match = false;
			}

			if(match && id != null) {
				if(!latestStoredServerLoggedDialogue.getId().equals(id)) match = false;
			}

			if(match) return latestStoredServerLoggedDialogue;
		}

		File[] userLogFiles;

		synchronized (LOCK) {
			userLogFiles = userLogDirectory.listFiles();
		}

		if(userLogFiles == null) throw new DatabaseException("Error retrieving file listing " +
				"from dialogue log directory for user '" + userId + "'.");

		Arrays.sort(userLogFiles);

		for(File f : userLogFiles) {
			List<ServerLoggedDialogue> serverLoggedDialogues = readSessionFile(f);
			if(serverLoggedDialogues != null) {
				for(ServerLoggedDialogue ld : serverLoggedDialogues) {
					boolean match = true;

					if(mustBeOngoing) {
						if(ld.isCancelled() || ld.isCompleted()) match = false;
					}

					if(match && dialogueName != null) {
						if(!ld.getDialogueName().equals(dialogueName)) match = false;
					}

					if(match && id != null) {
						if(!ld.getId().equals(id)) match = false;
					}

					if(match) return ld;
				}
			}
		}

		return null;
	}

	/**
	 * Private method used to read the JSON contents of a given dialogue log session file and return
	 * it as a List of ServerLoggedDialogue objects.
	 * @param sessionFile the File pointer to the dialogue log session file.
	 * @return a List of ServerLoggedDialogue objects.
	 * @throws DatabaseException in case of a read error.
	 * @throws IOException in case of a read error.
	 */
	private List<ServerLoggedDialogue> readSessionFile(File sessionFile)
			throws DatabaseException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		List<ServerLoggedDialogue> result;
		synchronized(LOCK) {
			try {
				result = mapper.readValue(sessionFile,
						new TypeReference<>() {
						});
			} catch (JsonProcessingException ex) {
				throw new DatabaseException("Failed to parse logged dialogues: "
						+ sessionFile.getAbsolutePath() + ": " + ex.getMessage(), ex);
			}
		}
		return result;
	}

}

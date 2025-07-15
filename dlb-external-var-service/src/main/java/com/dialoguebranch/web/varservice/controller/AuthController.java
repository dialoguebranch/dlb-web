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

package com.dialoguebranch.web.varservice.controller;

import com.dialoguebranch.web.varservice.*;
import com.dialoguebranch.web.varservice.auth.AuthenticationInfo;
import com.dialoguebranch.web.varservice.auth.basic.ServiceUserCredentials;
import com.dialoguebranch.web.varservice.auth.basic.ServiceUserFile;
import com.dialoguebranch.web.varservice.auth.jwt.JWTUtils;
import com.dialoguebranch.web.varservice.controller.schema.AccessTokenResponse;
import com.dialoguebranch.web.varservice.controller.schema.LoginParametersPayload;
import com.dialoguebranch.web.varservice.controller.schema.LoginResultPayload;
import com.dialoguebranch.web.varservice.exception.UnauthorizedException;
import nl.rrd.utils.AppComponents;
import nl.rrd.utils.datetime.DateTimeUtils;
import com.dialoguebranch.web.varservice.exception.BadRequestException;
import com.dialoguebranch.web.varservice.exception.ErrorCode;
import com.dialoguebranch.web.varservice.exception.HttpFieldError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.client.RestTemplate;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Controller for the /auth/... end-points of the Dialogue Branch External Variable Service.
 *
 * @author Harm op den Akker (Fruit Tree Labs)
 * @author Dennis Hofs (Roessingh Research and Development)
 */
@RestController
@RequestMapping(value = {"/v{version}/auth", "/auth"})
@Tag(name = "1. Authentication", description = "End-points related to Authentication.")
public class AuthController {

	@Autowired
	Application application;

	/** Used for executing QueryRunner operations in a thread-safe manner */
	private static final Object AUTH_LOCK = new Object();

	/** Used for writing logging information */
	private final Logger logger = AppComponents.getLogger(getClass().getSimpleName());

	/** Used to access configuration parameters */
	private final Configuration config = Configuration.getInstance();

	// -------------------------------------------------------- //
	// -------------------- Constructor(s) -------------------- //
	// -------------------------------------------------------- //

	/**
	 * Instances of this class are constructed through Spring.
	 */
	public AuthController() { }

	// ------------------------------------------------------------------ //
	// -------------------- END-POINT: "/auth/login" -------------------- //
	// ------------------------------------------------------------------ //

	/**
	 * Obtain an authentication token by logging in. Log in to the service by providing a username,
	 * password and indicating the desired duration of the authentication token in minutes. If you
	 * want to obtain an authentication token that does not expire, either provide '0' or 'never'
	 * as the value for '*tokenExpiration*'.
	 *
	 * @param request the {@link HttpServletRequest} that generated the call.
	 * @param response the {@link HttpServletResponse} that generated the call.
	 * @param version the API Version to use, e.g. '1'.
	 * @param loginParametersPayload the JSON payload containing the login parameters.
	 * @return a {@link LoginResultPayload} object after a successful login
	 * @throws Exception in case of a network error or unsuccessful login attempt.
	 */
	@Operation(summary = "Obtain an authentication token by logging in",
		description = "Log in to the service by providing a username, password and indicating " +
			"the desired duration of the authentication token in minutes. If you want to obtain " +
			"an authentication token that does not expire, either provide '0' or 'never' as the " +
			"value for '*tokenExpiration*'.")
	@RequestMapping(value="/login", method= RequestMethod.POST, consumes={
			MediaType.APPLICATION_JSON_VALUE })
	public LoginResultPayload login(
			HttpServletRequest request,
			HttpServletResponse response,

			@Parameter(hidden = true, description = "API Version to use, e.g. '1'")
			@PathVariable(value = "version")
			String version,

			@RequestBody
			LoginParametersPayload loginParametersPayload) throws Exception {

		// If no explicit protocol version is provided, assume the latest version
		if(version == null) version = ProtocolVersion.getLatestVersion().versionName();

		// Log this call to the service log
		if(loginParametersPayload != null) {
            logger.info("POST /v{}/auth/login for user '{}'.", version,
					loginParametersPayload.getUser());
		} else {
            logger.info("POST /v{}/auth/login with empty login parameters.", version);
			throw new BadRequestException("Missing login parameters in request body");
		}

		synchronized (AUTH_LOCK) {
			return QueryRunner.runQuery(
					(protocolVersion, user) -> doLogin(request, loginParametersPayload),
					version, null, response, "",application);
		}
	}

	private LoginResultPayload doLogin(HttpServletRequest request,
									   LoginParametersPayload loginParametersPayload)
			throws Exception {

		// Perform some validations on the request and login parameters, throwing a
		// BadRequestException in case of errors
		validateLoginParameters(request, loginParametersPayload);

		if(config.getKeycloakEnabled()) {
			logger.info("Keycloak authentication enabled.");
			return doLoginKeycloak(loginParametersPayload);
		} else {
			logger.info("Keycloak authentication disabled - using basic user management.");
			return doLoginNative(loginParametersPayload);
		}

	}

	private LoginResultPayload doLoginNative(LoginParametersPayload loginParametersPayload)
			throws UnauthorizedException {

		String user = loginParametersPayload.getUser();
		String password = loginParametersPayload.getPassword();
		Integer tokenExpiration = loginParametersPayload.getTokenExpiration();

		ServiceUserCredentials serviceUserCredentials = ServiceUserFile.findUser(user);
		String invalidError = "Username or password is invalid";
		if (serviceUserCredentials == null) {
			logger.info("Failed login attempt for user {}: user unknown.", user);
			throw new UnauthorizedException(ErrorCode.INVALID_CREDENTIALS, invalidError);
		}
		if (!serviceUserCredentials.getPassword().equals(password)) {
			logger.info("Failed login attempt for user {}: invalid credentials.", user);
			throw new UnauthorizedException(ErrorCode.INVALID_CREDENTIALS, invalidError);
		}
		logger.info("User {} logged in.", serviceUserCredentials.getUsername());

		Date expiration = null;

		ZonedDateTime now = DateTimeUtils.nowMs();

		if (tokenExpiration != null) {
			expiration = Date.from(now.plusMinutes(loginParametersPayload.getTokenExpiration())
					.toInstant());
		}

		AuthenticationInfo authenticationInfo = new AuthenticationInfo(
				user, Date.from(now.toInstant()), expiration);

		String token = JWTUtils.generateToken(authenticationInfo);

		return new LoginResultPayload(serviceUserCredentials.getUsername(), token);
	}

	private LoginResultPayload doLoginKeycloak(LoginParametersPayload loginParametersPayload)
			throws UnauthorizedException {

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		String keycloakLoginUrl = config.getKeycloakBaseUrl();
		if(!keycloakLoginUrl.endsWith("/")) keycloakLoginUrl += "/";
		keycloakLoginUrl += "realms/"
				+ config.getKeycloakRealm()
				+ "/protocol/openid-connect/token";

		logger.info("Redirecting login attempt to: {}", keycloakLoginUrl);

		MultiValueMap<String, String> requestParameters = new LinkedMultiValueMap<>();
		requestParameters.add("client_id",config.getKeycloakClientId());
		requestParameters.add("client_secret",config.getKeycloakClientSecret());
		requestParameters.add("username",loginParametersPayload.getUser());
		requestParameters.add("password",loginParametersPayload.getPassword());
		requestParameters.add("grant_type","password");

		ResponseEntity<AccessTokenResponse> response;

		try {
			HttpEntity<MultiValueMap<String,String>> entity = new HttpEntity<>(requestParameters,
					headers);
			response = restTemplate.exchange(
					keycloakLoginUrl,
					HttpMethod.POST,
					entity,
					AccessTokenResponse.class);
		} catch(Exception ex) {
			throw new UnauthorizedException(ErrorCode.KEYCLOAK_ERROR,
					"Error contacting Keycloak service.");
			// TODO: Catch additional details and add as fieldErrors to the UnauthorizedException
		}

		if (response.getStatusCode() == HttpStatus.OK) {
			logger.info("Call to Keycloak token end-point successful.");
			AccessTokenResponse keyCloakResponse = response.getBody();
			if (keyCloakResponse != null) {
				LoginResultPayload loginResultPayload = new LoginResultPayload();
				loginResultPayload.setToken(keyCloakResponse.getAccessToken());
				loginResultPayload.setUser(loginParametersPayload.getUser());
				return loginResultPayload;
			} else {
				logger.warn("Failed login attempt (empty response) for user {}.",
						loginParametersPayload.getUser());
				throw new UnauthorizedException(ErrorCode.KEYCLOAK_ERROR,
						"Invalid response from Keycloak service.");
			}

		} else {
			logger.warn("Failed login attempt for user {}: invalid request, status code {}.",
					loginParametersPayload.getUser(), response.getStatusCode());
			throw new UnauthorizedException(ErrorCode.KEYCLOAK_ERROR,
					"Keycloak service returned status code " + response.getStatusCode() + ".");
		}

	}

	/**
	 * Helper function for the doLogin method, validates the given {@code request} and
	 * {@code loginParametersPayload} parameters.
	 *
	 * @param request the HTTPRequest object (to retrieve authentication headers and optional body
	 * 	              parameters).
	 * @param loginParametersPayload the JSON payload containing the username, password and token
	 * 	 * 	                         expiration values.
	 * @throws BadRequestException in case of any error in the given {@link LoginParametersPayload}.
	 */
	private void validateLoginParameters(HttpServletRequest request,
										 LoginParametersPayload loginParametersPayload)
			throws BadRequestException {
		ControllerFunctions.validateForbiddenQueryParams(request, "user",
				"password");
		String user = loginParametersPayload.getUser();
		String password = loginParametersPayload.getPassword();
		Integer tokenExpiration = loginParametersPayload.getTokenExpiration();
		List<HttpFieldError> fieldErrors = new ArrayList<>();
		if (user == null || user.isEmpty()) {
			fieldErrors.add(new HttpFieldError("user",
					"Parameter 'user' not defined."));
		}
		if (password == null || password.isEmpty()) {
			fieldErrors.add(new HttpFieldError("password",
					"Parameter 'password' not defined."));
		}

		// If Keycloak is used for authentication, a specific token expiration can not actually be
		// set, so we're not going to complain about it here.
		if(!config.getKeycloakEnabled()) {

			if (tokenExpiration != null && tokenExpiration <= 0) {
				fieldErrors.add(new HttpFieldError("tokenExpiration",
						"Parameter 'tokenExpiration' must be greater than 0 or 'never'."));
			}

		}
		if (!fieldErrors.isEmpty()) {
			logger.info("Failed login attempt: {}", fieldErrors);
			throw BadRequestException.withMessageAndInvalidInput(
					"One or more login parameters were not correctly provided.",
					fieldErrors);
		}
	}

}

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

package com.dialoguebranch.web.service.controller;

import com.dialoguebranch.web.service.*;
import com.dialoguebranch.web.service.controller.schema.LoginParametersPayload;
import com.dialoguebranch.web.service.controller.schema.LoginResultPayload;
import com.dialoguebranch.web.service.exception.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import nl.rrd.utils.AppComponents;
import nl.rrd.utils.datetime.DateTimeUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Controller for the /auth/... end-points of the Dialogue Branch Web Service.
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
	 * Obtain an authentication token by logging in.
	 *
	 * <p>Log in to the service by providing a username, password and indicating the desired
	 * duration of the authentication token in minutes. If you want to obtain an authentication
	 * token that does not expire, either provide '0' or 'never' as the value for
	 * '*tokenExpiration*'. This method returns a JSON object containing the provided '*user*' name,
	 * the '*role*' of the user, and the JWT '*token*' that may be used to authenticate subsequent
	 * API calls.</p>
	 *
	 * @param request the HTTPRequest object (to retrieve authentication headers and optional body
	 *                parameters).
	 * @param response the HTTP response (to add header WWW-Authenticate in case of a 401
	 *                 Unauthorized error).
	 * @param version The API Version to use, e.g. '1'.
	 * @param loginParametersPayload the JSON payload containing the username, password and token
	 *                               expiration values.
	 * @return a {@link LoginResultPayload} object, containing the username, the corresponding role
	 *         and a JSON Web Token.
	 * @throws HttpException in case of a malformed request, or invalid login credentials
	 */
	@Operation(summary = "Obtain an authentication token by logging in.",
		description = "Log in to the service by providing a username, password and indicating " +
			"the desired duration of the authentication token in minutes. If you want to obtain " +
			"an authentication token that does not expire, either provide '0' or 'never' as the " +
			"value for '*tokenExpiration*'. This method returns a JSON object containing the " +
			"provided '*user*' name, the '*role*' of the user, and the JWT '*token*' that may be " +
			"used to authenticate subsequent API calls.")
	@RequestMapping(value="/login", method= RequestMethod.POST, consumes={
			MediaType.APPLICATION_JSON_VALUE })
	public LoginResultPayload login(
			HttpServletRequest request,
			HttpServletResponse response,

			@Parameter(hidden = true, description = "API Version to use, e.g. '1'")
			@PathVariable(value = "version")
			String version,

			@RequestBody
			LoginParametersPayload loginParametersPayload) throws HttpException {

		// If no versionName is provided, or versionName is empty, assume the latest version
		if (version == null || version.isEmpty()) {
			version = ProtocolVersion.getLatestVersion().versionName();
		}

		// Log this call to the service log
		if(loginParametersPayload != null) {
            logger.info("POST /v{}/auth/login for user '{}'.",
					version, loginParametersPayload.getUser());
		} else {
            logger.info("POST /v{}/auth/login with empty login parameters.", version);
			throw new BadRequestException("Missing login parameters in request body.");
		}

		synchronized (AUTH_LOCK) {
			return QueryRunner.runQuery(
					(protocolVersion, user) -> doLogin(request, loginParametersPayload),
					version, null, response, "", application);
		}
	}

	/**
	 * Performs the operation after a call to the /auth/login end-point.
	 *
	 * @param request the HTTPRequest object (to retrieve authentication headers and optional body
	 *                parameters).
	 * @param loginParametersPayload the JSON payload containing the username, password and token
	 * 	                             expiration values.
	 * @return a {@link LoginResultPayload} object, containing the username, the corresponding role
	 * 	       and a JSON Web Token.
	 * @throws BadRequestException in case of any error in the given {@link LoginParametersPayload}.
	 * @throws UnauthorizedException in case the username and passwords don't match.
	 */
	private LoginResultPayload doLogin(HttpServletRequest request,
									   LoginParametersPayload loginParametersPayload)
			throws BadRequestException, UnauthorizedException {

		ControllerFunctions.validateForbiddenQueryParams(request, "user", "password");
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
		if (tokenExpiration != null && tokenExpiration <= 0) {
			fieldErrors.add(new HttpFieldError("tokenExpiration",
					"Parameter 'tokenExpiration' must be greater than 0 or 'never'."));
		}
		if (!fieldErrors.isEmpty()) {
            logger.info("Failed login attempt: {}", fieldErrors);
			throw BadRequestException.withMessageAndInvalidInput(
					"One or more login parameters were not correctly provided.",
					fieldErrors);
		}
		UserCredentials userCredentials = UserFile.findUser(user);
		String invalidError = "Username or password is invalid";
		if (userCredentials == null) {
			logger.info("Failed login attempt for user {}: user unknown.", user);
			throw new UnauthorizedException(ErrorCode.INVALID_CREDENTIALS, invalidError);
		}
		if (!userCredentials.getPassword().equals(password)) {
			logger.info("Failed login attempt for user {}: invalid credentials.", user);
			throw new UnauthorizedException(ErrorCode.INVALID_CREDENTIALS, invalidError);
		}
		logger.info("User {} logged in.", userCredentials.getUsername());

		Date expiration = null;

		ZonedDateTime now = DateTimeUtils.nowMs();

		if (loginParametersPayload.getTokenExpiration() != null) {
			expiration = Date.from(now.plusMinutes(
					loginParametersPayload.getTokenExpiration()).toInstant());
		}

		AuthDetails details = new AuthDetails(user, Date.from(now.toInstant()), expiration);
		String token = AuthToken.createToken(details);

		return new LoginResultPayload(
				userCredentials.getUsername(),
				userCredentials.getRole(),
				token);
	}

	// --------------------------------------------------------------------- //
	// -------------------- END-POINT: "/auth/validate" -------------------- //
	// --------------------------------------------------------------------- //

	/**
	 * Validate a given authentication token.
	 *
	 * <p>If your client application has a stored authentication token you may use this method to
	 * check whether or not that is a valid token. This method will either return 'true', or throw
	 * an Authentication error.</p>
	 *
	 * @param request the HTTPRequest object (to retrieve authentication headers and optional body
	 *                parameters).
	 * @param response the HTTP response (to add header WWW-Authenticate in case of a 401
	 *                 Unauthorized error).
	 * @param version The API Version to use, e.g. '1'.
	 * @return 'true' if the token is correct, otherwise it will throw an exception.
	 * @throws UnauthorizedException if the given authentication token is not (or no longer) valid.
	 */
	@SecurityRequirement(name = "X-Auth-Token")
	@Operation(summary = "Validate a given authentication token.",
		description = "If your client application has a stored authentication token you may use" +
			"this method to check whether or not that is a valid token. This method will either" +
			"return 'true', or throw an Authentication error.")
	@RequestMapping(value="/validate", method= RequestMethod.POST)
	public boolean validate(
		HttpServletRequest request,
		HttpServletResponse response,

		@Parameter(hidden = true, description = "API Version to use, e.g. '1'")
		@PathVariable(value = "version")
		String version
	) throws UnauthorizedException {

		// If no versionName is provided, or versionName is empty, assume the latest version
		if (version == null || version.isEmpty()) {
			version = ProtocolVersion.getLatestVersion().versionName();
		}

		// Log this call to the service log
        logger.info("POST /v{}/auth/validate", version);

		synchronized (AUTH_LOCK) {
			QueryRunner.validateToken(request,application);
			return true;
		}
	}

}

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
import com.dialoguebranch.web.varservice.controller.schema.LoginParametersPayload;
import com.dialoguebranch.web.varservice.controller.schema.LoginResultPayload;
import com.dialoguebranch.web.varservice.exception.UnauthorizedException;
import nl.rrd.utils.AppComponents;
import nl.rrd.utils.datetime.DateTimeUtils;
import nl.rrd.utils.exception.ParseException;
import nl.rrd.utils.http.URLParameters;
import com.dialoguebranch.web.varservice.exception.BadRequestException;
import com.dialoguebranch.web.varservice.exception.ErrorCode;
import com.dialoguebranch.web.varservice.exception.HttpFieldError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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

	private static final Object AUTH_LOCK = new Object();
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
			logger.info("POST /v" + version + "/auth/login for user '" +
					loginParametersPayload.getUser() + "'.");
		} else {
			logger.info("POST /v" + version + "/auth/login with empty login parameters.");
			throw new BadRequestException("Missing login parameters in request body");
		}

		synchronized (AUTH_LOCK) {
			return QueryRunner.runQuery(
					(protocolVersion, user) -> doLogin(request, loginParametersPayload),
					version, null, response, "");
		}
	}

	private LoginResultPayload doLogin(HttpServletRequest request,
									   LoginParametersPayload loginParametersPayload)
			throws Exception {
		validateForbiddenQueryParams(request, "user", "password");

		String user = loginParametersPayload.getUser();
		String password = loginParametersPayload.getPassword();
		Integer tokenExpiration = loginParametersPayload.getTokenExpiration();

		List<HttpFieldError> fieldErrors = new ArrayList<>();
		if (user == null || user.isEmpty()) {
			fieldErrors.add(new HttpFieldError("user",
					"Parameter \"user\" not defined"));
		}
		if (password == null || password.isEmpty()) {
			fieldErrors.add(new HttpFieldError("password",
					"Parameter \"password\" not defined"));
		}
		if (tokenExpiration != null && tokenExpiration <= 0) {
			fieldErrors.add(new HttpFieldError("tokenExpiration",
					"Parameter \"tokenExpiration\" must be greater than 0 or \"never\""));
		}
		if (!fieldErrors.isEmpty()) {
			logger.info("Failed login attempt: " + fieldErrors);
			throw BadRequestException.withInvalidInput(fieldErrors);
		}
		UserCredentials credentials = UserFile.findUser(user);
		String invalidError = "Username or password is invalid";
		if (credentials == null) {
			logger.info("Failed login attempt for user {}: user unknown",
					user);
			throw new UnauthorizedException(ErrorCode.INVALID_CREDENTIALS,
					invalidError);
		}
		if (!credentials.password().equals(password)) {
			logger.info("Failed login attempt for user {}: invalid credentials",
					user);
			throw new UnauthorizedException(ErrorCode.INVALID_CREDENTIALS,
					invalidError);
		}
		logger.info("User {} logged in", credentials.username());

		Date expiration = null;

		ZonedDateTime now = DateTimeUtils.nowMs();

		if (loginParametersPayload.getTokenExpiration() != null) {
			expiration = Date.from(now.plusMinutes(
					loginParametersPayload.getTokenExpiration()).toInstant());
		}

		AuthDetails details = new AuthDetails(user, Date.from(now.toInstant()),
				expiration);
		String token = AuthToken.createToken(details);

		return new LoginResultPayload(credentials.username(), token);
	}

	private void validateForbiddenQueryParams(HttpServletRequest request,
			String... paramNames) throws BadRequestException {
		if (request.getQueryString() == null)
			return;
		Map<String,String> params;
		try {
			params = URLParameters.parseParameterString(
					request.getQueryString());
		} catch (ParseException ex) {
			throw new BadRequestException(ex.getMessage());
		}
		for (String name : paramNames) {
			if (params.containsKey(name)) {
				throw new BadRequestException(
					"Query parameters not accepted, parameters must be set in the request body");
			}
		}
	}

}

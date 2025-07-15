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

package com.dialoguebranch.web.varservice;

import com.dialoguebranch.web.varservice.auth.AuthenticationInfo;
import com.dialoguebranch.web.varservice.auth.basic.ServiceUserCredentials;
import com.dialoguebranch.web.varservice.auth.basic.ServiceUserFile;
import com.dialoguebranch.web.varservice.auth.jwt.JWTUtils;
import com.dialoguebranch.web.varservice.exception.*;
import nl.rrd.utils.AppComponents;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This class can run queries. It can validate an authentication token.
 * 
 * @author Dennis Hofs
 * @author Harm op den Akker
 */
public class QueryRunner {

	/**
	 * This class is used in a static context.
	 */
	public QueryRunner() { }

	/**
	 * Runs a query on the authentication database. If the HTTP request is specified, it will
	 * validate the authentication token. If there is no token in the request, or the token is empty
	 * or invalid, it throws an HttpException with 401 Unauthorized. If the request is null, it will
	 * not validate anything. This can be used for a login or signup.
	 *
	 * @param <T> the type of the query to be executed
	 * @param query the query
	 * @param versionName the protocol version name (see {@link ProtocolVersion
	 * ProtocolVersion})
	 * @param request the HTTP request or null
	 * @param response the HTTP response (to add header WWW-Authenticate in
	 *                 case of 401 Unauthorized)
	 * @param requestUser the "DialogueBranch user" for which this query should be run, or ""
	 *                   if this should be for the currently authenticated user
	 * @return the query result
	 * @throws HttpException if the query should return an HTTP error status
	 * @throws Exception if an unexpected error occurs. This results in HTTP
	 * error status 500 Internal Server Error.
	 */
	public static <T> T runQuery(AuthQuery<T> query, String versionName, HttpServletRequest request,
			HttpServletResponse response, String requestUser, Application application)
			throws HttpException, Exception {

		// Verify the protocol version
		ProtocolVersion version;
		try {
			version = ProtocolVersion.forVersionName(versionName);
		} catch (IllegalArgumentException ex) {
			throw new BadRequestException("Unknown protocol version: " + versionName);
		}

		try {
			AuthenticationInfo authenticationInfo;

			// In case of a login call
			if(request == null) {
				return query.runQuery(version, "");
			} else {
				authenticationInfo = validateToken(request, application);
			}

			if(requestUser == null || requestUser.isEmpty()) {
				throw new BadRequestException("No user id specified for the request");

			// For the next cases, we need the authentication info to not be null
			} else if(authenticationInfo != null) {

				// If Keycloak is enabled, the authenticated user can only request data of himself
				// So, the "requestUser" must be the same as the username in the authentication
				// details
				if(application.getConfiguration().getKeycloakEnabled()) {
					if (authenticationInfo.getUsername().equals(requestUser)) {
						return query.runQuery(version, authenticationInfo.getUsername());
					} else {
						throw new UnauthorizedException("Authenticated user '" +
							authenticationInfo.getUsername() + "' is not authorized to access " +
							"data of requested user '"+requestUser+"'.");
					}
				} else {

					// Otherwise, the authenticated user is a "service-user" as specified in the
					// service-users.xml and he/she/it has access to any real user data
					return query.runQuery(version, authenticationInfo.getUsername());
				}

			// Otherwise, something is wrong
			} else {
				throw new UnauthorizedException("Unknown user.");
			}
		} catch (UnauthorizedException ex) {
			response.addHeader("WWW-Authenticate", "None");
			throw ex;
		} catch (HttpException ex) {
			throw ex;
		} catch (Exception ex) {
			Logger logger = AppComponents.getLogger(QueryRunner.class.getSimpleName());
			logger.error("Internal Server Error: {}", ex.getMessage(), ex);
			throw new InternalServerErrorException();
		}
	}

	/**
	 * Validates the authentication token in the specified HTTP request. If no token is specified,
	 * or the token is empty or invalid, it will throw an HttpException with 401 Unauthorized.
	 * Otherwise, it will return the {@link AuthenticationInfo} object representing the information
	 * of the authenticated user.
	 *
	 * @param request the HTTP request
	 * @param application the {@link Application} context used to access {@link
	 *                    ServiceUserCredentials} in a non-static way.
	 * @return the {@link AuthenticationInfo} for the authenticated user
	 * @throws UnauthorizedException if no token is specified, or the token is empty or invalid
	 */
	public static AuthenticationInfo validateToken(HttpServletRequest request,
												   Application application)
			throws UnauthorizedException {
		Logger logger = AppComponents.getLogger(QueryRunner.class.getSimpleName());

		String token = request.getHeader("X-Auth-Token");

		if (token != null) {

			if (token.trim().isEmpty()) {
				logger.info("Invalid authentication token: token empty");
				throw new UnauthorizedException(ErrorCode.AUTH_TOKEN_INVALID,
						"Authentication token invalid");
			}

			if(application.getConfiguration().getKeycloakEnabled())
				return validateKeycloakToken(token, application);
			else
				return validateDefaultToken(token);
		}

		throw new UnauthorizedException(ErrorCode.AUTH_TOKEN_NOT_FOUND,
				"Authentication token not found");
	}

	/**
	 * Validates the given token from request header, using the built-in user management and token
	 * system. If it's empty or invalid, it will throw an HttpException with 401 Unauthorized.
	 * Otherwise, it will return an {@link AuthenticationInfo} object representing the information
	 * of the authenticated user.
	 *
	 * @param token the authentication token (not null)
	 * @return the {@link AuthenticationInfo}, representing the authenticated user
	 * @throws UnauthorizedException if the token is empty or invalid
	 */
	private static AuthenticationInfo validateDefaultToken(String token)
			throws UnauthorizedException {
		Logger logger = AppComponents.getLogger(QueryRunner.class.getSimpleName());

		AuthenticationInfo authenticationInfo;
		try {
			authenticationInfo = JWTUtils.isTokenValid(token);
		} catch (ExpiredJwtException ex) {
			throw new UnauthorizedException(ErrorCode.AUTH_TOKEN_EXPIRED,
					"Authentication token expired");
		} catch (JwtException ex) {
			logger.info("Invalid authentication token: failed to parse: {}", ex.getMessage());
			throw new UnauthorizedException(ErrorCode.AUTH_TOKEN_INVALID,
					"Authentication token invalid");
		}

		ServiceUserCredentials userCredentials = ServiceUserFile.findUser(
				authenticationInfo.getUsername());
		if (userCredentials == null) {
			logger.info("Invalid authentication token: user not found: {}",
					authenticationInfo.getUsername());
			throw new UnauthorizedException(ErrorCode.AUTH_TOKEN_INVALID,
					"Authentication token invalid");
		}

		if (authenticationInfo.getExpiration() != null &&
				authenticationInfo.getExpiration().getTime() < System.currentTimeMillis()) {
			throw new UnauthorizedException(ErrorCode.AUTH_TOKEN_EXPIRED,
					"Authentication token expired");
		}

		return authenticationInfo;
	}

	/**
	 * Validates the given Keycloak token from request header. If it's empty or invalid, it will
	 * throw an HttpException with 401 Unauthorized. Otherwise, it will return the user object for
	 * the authenticated user.
	 *
	 * @param token the authentication token (not null)
	 * @param application the {@link Application} context used to access the Keycloak manager in a
	 *                    non-static way.
	 * @return the {@link AuthenticationInfo} object representing the authenticated user
	 * @throws UnauthorizedException if the token is empty or invalid
	 */
	private static AuthenticationInfo validateKeycloakToken(String token, Application application)
			throws UnauthorizedException{
		return application.getKeycloakManager().validateToken(token);
	}

}

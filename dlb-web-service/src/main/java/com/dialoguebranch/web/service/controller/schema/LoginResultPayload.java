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

package com.dialoguebranch.web.service.controller.schema;

import com.dialoguebranch.web.service.controller.AuthController;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * A {@link LoginResultPayload} is returned by the /auth/login end-point as handled by the {@link
 * AuthController} in case of a successful login and contains the username and JSON Web Token, which
 * can be serialized / deserialized to the following JSON Format:
 * <pre>
 * {
 *   "user": "john",
 *   "role": "admin",
 *   "token": "See <a href="https://jwt.io/">jwt.io</a>"
 * }
 * </pre>
 *
 * @author Harm op den Akker (Fruit Tree Labs)
 */
public class LoginResultPayload {

	@Schema(description = "Username associated with this authentication token",
			example = "john")
	private String user;

	@Schema(description = "The role of the authenticated user",
			example = "admin")
	private String role;

	@Schema(description = "The JSON Web Token that was generated for the user",
			example = "See https://jwt.io/")
	private String token;



	// --------------------------------------------------------
	// -------------------- Constructor(s) --------------------
	// --------------------------------------------------------

	/**
	 * Creates an empty instance of a {@link LoginResultPayload}.
	 */
	public LoginResultPayload() { }

	/**
	 * Creates an instance of a {@link LoginResultPayload} with a given {@code user} and {@code
	 * token}.
	 *
	 * @param user the username of the user that performed a successful login.
	 * @param role the role of the user associated with this {@link LoginResultPayload}.
	 * @param token the JSON Web Token generated for the user as a {@link String}.
	 */
	public LoginResultPayload(String user, String role, String token) {
		this.user = user;
		this.role = role;
		this.token = token;
	}

	// -------------------------------------------------------------
	// -------------------- Getters and Setters --------------------
	// -------------------------------------------------------------

	/**
	 * Returns the username of the user that performed a successful login.
	 * @return the username of the user that performed a successful login.
	 */
	public String getUser() {
		return this.user;
	}

	/**
	 * Sets the username of the user that performed a successful login.
	 * @param user the username of the user that performed a successful login.
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * Returns the role of the user associated with this {@link LoginResultPayload}.
	 * @return the role of the user associated with this {@link LoginResultPayload}.
	 */
	public String getRole() {
		return this.role;
	}

	/**
	 * Sets the role of the user associated with this {@link LoginResultPayload}.
	 * @param role the role of the user associated with this {@link LoginResultPayload}.
	 */
	public void setRole(String role) {
		this.role = role;
	}

	/**
	 * Returns the JSON Web Token generated for the user as a {@link String}.
	 * @return the JSON Web Token generated for the user as a {@link String}.
	 */
	public String getToken() {
		return token;
	}

	/**
	 * Sets the JSON Web Token generated for the user as a {@link String}.
	 * @param token the JSON Web Token generated for the user as a {@link String}.
	 */
	public void setToken(String token) {
		this.token = token;
	}

}

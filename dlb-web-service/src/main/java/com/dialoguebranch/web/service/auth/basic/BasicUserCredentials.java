/*
 *
 *                Copyright (c) 2023-2025 Fruit Tree Labs (www.fruittreelabs.com)
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

package com.dialoguebranch.web.service.auth.basic;

public class BasicUserCredentials {

	public static final String USER_ROLE_CLIENT = "client";
	public static final String USER_ROLE_EDITOR = "editor";
	public static final String USER_ROLE_ADMIN = "admin";

	private final String username;
	private String password = null;
	private final String[] roles;

	public BasicUserCredentials(String username, String password, String[] roles) {
		this.username = username;
		this.password = password;
		this.roles = roles;
	}

	public BasicUserCredentials(String username, String[] roles) {
		this.username = username;
		this.roles = roles;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String[] getRoles() {
		return roles;
	}

	/**
	 * Returns the roles associated with this user as a comma-separated String.
	 *
	 * @return the roles associated with this user as a comma-separated String.
	 */
	public String getCommaSeparatedRolesString() {
		StringBuilder result = new StringBuilder();

		for(int i=0; i<roles.length; i++) {
			result.append(roles[i]);
			if(i+1 < roles.length) result.append(",");
		}

		return result.toString();
	}

}

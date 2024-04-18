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

package com.dialoguebranch.web.varservice;

import java.util.Date;

/**
 * The authentication details that are included in a JWT token. It contains the
 * username of the authenticated user, the date/time when the JWT token was
 * issued, and the date/time when the JWT token expires.
 * 
 * @author Dennis Hofs (Roessingh Research and Development)
 */
public class AuthDetails {
	private final String subject;
	private final Date issuedAt;
	private final Date expiration;

	// -------------------------------------------------------- //
	// -------------------- Constructor(s) -------------------- //
	// -------------------------------------------------------- //

	/**
	 * Constructs a new instance.
	 * 
	 * @param subject the username of the authenticated user
	 * @param issuedAt the date/time when the JWT token was issued, with precision of seconds.
	 *                 Any milliseconds are discarded.
	 * @param expiration the date/time when the JWT token expires, with
	 * precision of seconds. Any milliseconds are discarded.
	 */
	public AuthDetails(String subject, Date issuedAt, Date expiration) {
		this.subject = subject;
		long seconds = issuedAt.getTime() / 1000;
		this.issuedAt = new Date(seconds * 1000);
		if (expiration == null) {
			this.expiration = null;
		} else {
			seconds = expiration.getTime() / 1000;
			this.expiration = new Date(seconds * 1000);
		}
	}

	// ----------------------------------------------------------- //
	// -------------------- Getters & Setters -------------------- //
	// ----------------------------------------------------------- //

	/**
	 * Returns the username of the authenticated user.
	 * 
	 * @return the username of the authenticated user
	 */
	public String getSubject() {
		return subject;
	}
	
	/**
	 * The date/time when the JWT token was issued, with precision of seconds.
	 * 
	 * @return the date/time when the JWT token was issued, with precision of
	 * seconds
	 */
	public Date getIssuedAt() {
		return issuedAt;
	}

	/**
	 * Returns the date/time when the JWT token expires, with precision of
	 * seconds.
	 * 
	 * @return the date/time when the JWT token expires, with precision of
	 * seconds
	 */
	public Date getExpiration() {
		return expiration;
	}

}

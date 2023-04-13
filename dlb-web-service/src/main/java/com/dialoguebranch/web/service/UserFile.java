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

package com.dialoguebranch.web.service;

import nl.rrd.utils.AppComponents;
import nl.rrd.utils.exception.ParseException;
import nl.rrd.utils.xml.AbstractSimpleSAXHandler;
import nl.rrd.utils.xml.SimpleSAXParser;
import org.slf4j.Logger;
import org.xml.sax.Attributes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserFile {

	public static UserCredentials findUser(String username) {
		List<UserCredentials> users;
		try {
			users = read();
		} catch (ParseException | IOException ex) {
			throw new RuntimeException("Failed to read users.xml: " +
					ex.getMessage(), ex);
		}
		String lower = username.toLowerCase();
		for (UserCredentials user : users) {
			if (user.getUsername().toLowerCase().equals(lower))
				return user;
		}
		return null;
	}

	public static List<UserCredentials> read() throws ParseException,
			IOException {
		Configuration config = Configuration.getInstance();
		File dataDir = new File(config.get(Configuration.DATA_DIR));
		File usersFile = new File(dataDir, "users.xml");
		SimpleSAXParser<List<UserCredentials>> parser = new SimpleSAXParser<>(
				new XMLHandler());
		return parser.parse(usersFile);
	}

	private static class XMLHandler extends
			AbstractSimpleSAXHandler<List<UserCredentials>> {
		private final List<UserCredentials> users = new ArrayList<>();

		@Override
		public void startElement(String name, Attributes attributes, List<String> parents)
				throws ParseException {
			if (parents.size() == 0) {
				if (!name.equals("users")) {
					throw new ParseException("Expected element \"users\", found: " + name);
				}
			} else if (parents.size() == 1) {
				if (!name.equals("user")) {
					throw new ParseException("Expected element \"user\", found: " + name);
				}
				startUser(attributes);
			}
		}

		private void startUser(Attributes attributes) throws ParseException {
			String username = readAttribute(attributes, "username").trim();
			if (username.length() == 0) {
				throw new ParseException("Empty value in attribute \"username\"");
			}
			String password = readAttribute(attributes, "password");
			if (password.length() == 0) {
				throw new ParseException("Empty value in attribute \"password\"");
			}
			String role;
			try {
				role = readAttribute(attributes, "role");

				if (!(role.equalsIgnoreCase(UserCredentials.USER_ROLE_USER) ||
						role.equalsIgnoreCase(UserCredentials.USER_ROLE_ADMIN))) {
					throw new ParseException(
							"Invalid specification for \"role\": " + role);
				}
			} catch (ParseException pe) {
				role = UserCredentials.USER_ROLE_USER;
				Logger logger = AppComponents.getLogger(UserFile.class.getSimpleName());
				logger.warn("Warning while reading users.xml file: User role not defined for user '"
						+username+"', assuming role '"+role+"'.");
			}
			users.add(new UserCredentials(username, password, role));
		}

		@Override
		public void endElement(String name, List<String> parents) {
		}

		@Override
		public void characters(String ch, List<String> parents) {
		}

		@Override
		public List<UserCredentials> getObject() {
			return users;
		}
	}
}

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

/**
 * A {@link UserFile} objects represents the contents of the users.xml file that contains the
 * credentials for valid users of the service.
 *
 * @author Harm op den Akker (Fruit Tree Labs)
 */
public class UserFile {

	// -------------------------------------------------------- //
	// -------------------- Constructor(s) -------------------- //
	// -------------------------------------------------------- //

	/**
	 * This class may be used in a static way.
	 */
	public UserFile() { }

	// ------------------------------------------------------- //
	// -------------------- Other Methods -------------------- //
	// ------------------------------------------------------- //

	/**
	 * Retrieve the {@link UserCredentials} matching the given {@code username}.
	 *
	 * @param username the username of the user for whom to search.
	 * @return the {@link UserCredentials} object matching the user, or {@code null} if none is
	 * 		   found.
	 */
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
			if (user.username().toLowerCase().equals(lower))
				return user;
		}
		return null;
	}

	/**
	 * Read the full list of configured users and return it as a {@link List} of {@link
	 * UserCredentials} objects.
	 *
	 * @return the full list of existing users for this service.
	 * @throws ParseException in case of an error parsing the users.xml file.
	 * @throws IOException in case of an error in parsing the users.xml file.
	 */
	private static List<UserCredentials> read() throws ParseException, IOException {
		Configuration config = Configuration.getInstance();
		File dataDir = new File(config.get(Configuration.DATA_DIR));
		File usersFile = new File(dataDir, "users.xml");
		SimpleSAXParser<List<UserCredentials>> parser = new SimpleSAXParser<>(new XMLHandler());
		return parser.parse(usersFile);
	}

	/**
	 * Implementation of an {@link AbstractSimpleSAXHandler} for parsing users.xml files.
	 */
	private static class XMLHandler extends AbstractSimpleSAXHandler<List<UserCredentials>> {
		private final List<UserCredentials> users = new ArrayList<>();

		@Override
		public void startElement(String name, Attributes attributes,
				List<String> parents) throws ParseException {
			if (parents.isEmpty()) {
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
			if (username.isEmpty()) {
				throw new ParseException("Empty value in attribute \"username\"");
			}
			String password = readAttribute(attributes, "password");
			if (password.isEmpty()) {
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
		public void endElement(String name, List<String> parents) { }

		@Override
		public void characters(String ch, List<String> parents) { }

		@Override
		public List<UserCredentials> getObject() {
			return users;
		}
	}

}

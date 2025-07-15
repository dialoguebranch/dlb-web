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

package com.dialoguebranch.web.varservice.auth.basic;

import com.dialoguebranch.web.varservice.Configuration;
import nl.rrd.utils.exception.ParseException;
import nl.rrd.utils.xml.AbstractSimpleSAXHandler;
import nl.rrd.utils.xml.SimpleSAXParser;
import org.xml.sax.Attributes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link ServiceUserFile} objects represents the contents of the service-users.xml file that
 * contains the credentials for valid users of the Dialogue Branch External Variable Service. These
 * users are used when the Keycloak authentication is disabled. After a successful login to the
 * /auth/login end-point, a token is generated that allows access to all end-points of the service.
 * I.e. these users identify a Dialogue Branch Web Service (or other) service instance, and have
 * full access to any user data stored in this External Variable Service.
 *
 * @author Harm op den Akker (Fruit Tree Labs)
 */
public class ServiceUserFile {

	// -------------------------------------------------------- //
	// -------------------- Constructor(s) -------------------- //
	// -------------------------------------------------------- //

	/**
	 * This class may be used in a static way.
	 */
	public ServiceUserFile() { }

	// ------------------------------------------------------- //
	// -------------------- Other Methods -------------------- //
	// ------------------------------------------------------- //

	/**
	 * Retrieve the {@link ServiceUserCredentials} matching the given {@code username}.
	 *
	 * @param username the username of the user for whom to search.
	 * @return the {@link ServiceUserCredentials} object matching the user, or {@code null} if none
	 *         is found.
	 */
	public static ServiceUserCredentials findUser(String username) {
		List<ServiceUserCredentials> users;
		try {
			users = read();
		} catch (ParseException | IOException ex) {
			throw new RuntimeException("Failed to read service-users.xml: " + ex.getMessage(), ex);
		}
		String lower = username.toLowerCase();
		for (ServiceUserCredentials user : users) {
			if (user.getUsername().toLowerCase().equals(lower))
				return user;
		}
		return null;
	}

	/**
	 * Read the full list of configured users and return it as a {@link List} of {@link
	 * ServiceUserCredentials} objects.
	 *
	 * @return the full list of existing users for this service.
	 * @throws ParseException in case of an error parsing the users.xml file.
	 * @throws IOException in case of an error in parsing the users.xml file.
	 */
	private static List<ServiceUserCredentials> read() throws ParseException, IOException {
		Configuration config = Configuration.getInstance();
		File dataDir = new File(config.get(Configuration.DATA_DIR));
		File serviceUsersFile = new File(dataDir, "service-users.xml");
		SimpleSAXParser<List<ServiceUserCredentials>> parser
				= new SimpleSAXParser<>(new XMLHandler());
		return parser.parse(serviceUsersFile);
	}

	/**
	 * Implementation of an {@link AbstractSimpleSAXHandler} for parsing service-users.xml files.
	 */
	private static class XMLHandler extends AbstractSimpleSAXHandler<List<ServiceUserCredentials>> {
		private final List<ServiceUserCredentials> users = new ArrayList<>();

		@Override
		public void startElement(String name, Attributes attributes,
				List<String> parents) throws ParseException {
			if (parents.isEmpty()) {
				if (!name.equals("service-users")) {
					throw new ParseException("Expected element \"service-users\", found: " + name);
				}
			} else if (parents.size() == 1) {
				if (!name.equals("service-user")) {
					throw new ParseException("Expected element \"service-user\", found: " + name);
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

			users.add(new ServiceUserCredentials(username, password));
		}

		@Override
		public void endElement(String name, List<String> parents) { }

		@Override
		public void characters(String ch, List<String> parents) { }

		@Override
		public List<ServiceUserCredentials> getObject() {
			return users;
		}
	}

}

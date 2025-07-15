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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serial;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Properties;

import nl.rrd.utils.AppComponent;

/**
 * Configuration of the DialogueBranch External Variable Service Dummy. This is initialized
 * from resources service.properties and deployment.properties. Known property keys are defined
 * as constants in this class.
 * 
 * @author Dennis Hofs (Roessingh Research and Development)
 * @author Harm op den Akker (Fruit Tree Labs)
 */
@AppComponent
public class Configuration extends LinkedHashMap<String,String> {

	@Serial
	private static final long serialVersionUID = 1L;

	/** Key name for the "version" parameter. */
	public static final String VERSION = "version";

	/** Key name for the "buildTime" parameter. */
	public static final String BUILD_TIME = "buildTime";

	/** Key name for the "baseUrl" parameter. */
	public static final String BASE_URL = "baseUrl";

	/** Key name for the "jwtSecretKey" parameter. */
	public static final String JWT_SECRET_KEY = "jwtSecretKey";

	/** Key name for the "dataDir" parameter. */
	public static final String DATA_DIR = "dataDir";

	// ---------- KeyCloak Configuration

	/**
	 * Name of the config parameter indicating whether the service should use an external Keycloak
	 * service for handling user authorization.
	 */
	public static final String KEYCLOAK_ENABLED = "keycloakEnabled";

	public static final String KEYCLOAK_BASEURL = "keycloakBaseUrl";

	public static final String KEYCLOAK_REALM = "keycloakRealm";

	public static final String KEYCLOAK_CLIENT_ID = "keycloakClientId";

	public static final String KEYCLOAK_CLIENT_SECRET = "keycloakClientSecret";

	private static final Object LOCK = new Object();
	private static Configuration instance = null;

	// -------------------------------------------------------- //
	// -------------------- Constructor(s) -------------------- //
	// -------------------------------------------------------- //

	/**
	 * Returns the configuration. At startup of the service it should be
	 * initialized with {@link #loadProperties(URL) loadProperties()}.
	 * 
	 * @return the configuration
	 */
	public static Configuration getInstance() {
		synchronized (LOCK) {
			if (instance == null)
				instance = new Configuration();
			return instance;
		}
	}

	/**
	 * This private constructor is used in {@link #getInstance()
	 * getInstance()}.
	 */
	private Configuration() {
	}

	// ------------------------------------------------------- //
	// -------------------- Other Methods -------------------- //
	// ------------------------------------------------------- //

	/**
	 * Loads the resource service.properties or deployment.properties into this
	 * configuration. This should only be called once at startup of the
	 * service.
	 * 
	 * @param url the URL of service.properties or deployment.properties
	 * @throws IOException if a reading error occurs
	 */
	public void loadProperties(URL url) throws IOException {
		Properties props = new Properties();
		if(url == null) throw new IOException("Cannot load properties file from null.");
		try (Reader reader = new InputStreamReader(url.openStream(),
				StandardCharsets.UTF_8)) {
			props.load(reader);
		}
		for (String name : props.stringPropertyNames()) {
			put(name, props.getProperty(name));
		}
	}

	/**
	 * Returns a date-time {@link String} representing the date and time that this version
	 * of the deployed web service was built.
	 * @return the build-time as a date-time {@link String}.
	 */
	public String getBuildTime() {
		if(containsKey(BUILD_TIME)) {
			String returnValue = get(BUILD_TIME);
			if(returnValue != null) return returnValue;
		}
		return "";
	}

	/**
	 * Returns the secret key used for encoding/decoding the JSON Web Tokens.
	 *
	 * @return the secret key used for encoding/decoding the JSON Web Tokens.
	 */
	public String getJwtSecretKey() {
		if(get(JWT_SECRET_KEY) == null) return "";
		else return get(JWT_SECRET_KEY);
	}

	// -------------------------------------------------------------------------
	// -------------------- Getters: Keycloak Configuration --------------------
	// -------------------------------------------------------------------------

	public boolean getKeycloakEnabled() {
		return Boolean.parseBoolean(get(KEYCLOAK_ENABLED));
	}

	public String getKeycloakBaseUrl() {
		if(get(KEYCLOAK_BASEURL) == null) return "";
		else return get(KEYCLOAK_BASEURL);
	}

	public String getKeycloakRealm() {
		if(get(KEYCLOAK_REALM) == null) return "";
		else return get(KEYCLOAK_REALM);
	}

	public String getKeycloakClientId() {
		if(get(KEYCLOAK_CLIENT_ID) == null) return "";
		else return get(KEYCLOAK_CLIENT_ID);
	}

	public String getKeycloakClientSecret() {
		if(get(KEYCLOAK_CLIENT_SECRET) == null) return "";
		else return get(KEYCLOAK_CLIENT_SECRET);
	}

}

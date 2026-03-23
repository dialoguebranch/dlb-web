/*
 *
 *                Copyright (c) 2023-2026 Fruit Tree Labs (www.fruittreelabs.com)
 *
 *
 *     This material is part of the Dialogue Branch Platform, and is covered by the MIT License
 *                                        as outlined below.
 *
 *                                            ----------
 *
 * Copyright (c) 2023-2026 Fruit Tree Labs (www.fruittreelabs.com)
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

import nl.rrd.utils.AppComponent;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serial;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Configuration of the Dialogue Branch External Variable Service Dummy. This is initialized from
 * resources service.properties and deployment.properties. Known property keys are defined as
 * constants in this class.
 * 
 * @author Dennis Hofs
 * @author Harm op den Akker
 */
@AppComponent
public class Configuration extends LinkedHashMap<String,String> {

	@Serial
	private static final long serialVersionUID = 1L;

	private static final Object LOCK = new Object();

	private static Configuration instance = null;

	// ------------------------------------------------------------- //
	// -------------------- Known property keys -------------------- //
	// ------------------------------------------------------------- //

	// ---------- General

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

	// ---------- Authentication Configuration

	/**
	 * Name of the config parameter indicating the authentication service to use, which can be set
	 * to either "keycloak", or "native".
	 */
	public static final String AUTH_SERVICE = "authService";

	/**
	 * Constant defining the value of AUTH_SERVICE indicating that Keycloak should be used.
	 */
	public static final String AUTH_SERVICE_KEYCLOAK = "keycloak";

	/**
	 * Constant defining the value of AUTH_SERVICE indicating that the Native auth system should be
	 * used.
	 */
	public static final String AUTH_SERVICE_NATIVE = "native";

	/**
	 * Name of the config parameter to define the Keycloak base url.
	 */
	public static final String AUTH_KEYCLOAK_BASEURL = "authKeycloakBaseUrl";

	/**
	 * Name of the config parameter to define the Keycloak realm.
	 */
	public static final String AUTH_KEYCLOAK_REALM = "authKeycloakRealm";

	/**
	 * Name of the config parameter to define the Keycloak client ID.
	 */
	public static final String AUTH_KEYCLOAK_CLIENT_ID = "authKeycloakClientId";

	/**
	 * Name of the config parameter to define the Keycloak Client Secret.
	 */
	public static final String AUTH_KEYCLOAK_CLIENT_SECRET = "authKeycloakClientSecret";

	/**
	 * Name of the config parameter to define the Native "Service User" name.
	 */
	public static final String AUTH_NATIVE_SERVICE_USER = "authNativeServiceUser";

	/**
	 * Name of the config parameter to define the Native "Service User" password.
	 */
	public static final String AUTH_NATIVE_SERVICE_PASSWORD = "authNativeServicePassword";

	/**
	 * Name of the config parameter that defines the JSON Web Token (JWT) 'secret' used to encrypt
	 * and decrypt ACCESS TOKENS.
	 */
	public static final String AUTH_NATIVE_JWT_ACCESS_TOKEN_SECRET = "authNativeJwtAccessTokenSecret";

	/**
	 * Name of the config parameter that indicates how long access tokens should be active.
	 */
	public static final String AUTH_NATIVE_ACCESS_TOKEN_EXPIRATION_SECONDS = "authNativeAccessTokenExpirationSeconds";

	/**
	 * Name of the config parameter that defines the JSON Web Token (JWT) 'secret' used to encrypt
	 * and decrypt REFRESH TOKENS.
	 */
	public static final String AUTH_NATIVE_JWT_REFRESH_TOKEN_SECRET = "authNativeJwtRefreshTokenSecret";

	/**
	 * Name of the config parameter that indicates how long refresh tokens should be active
	 * (typically much longer).
	 */
	public static final String AUTH_NATIVE_REFRESH_TOKEN_EXPIRATION_SECONDS = "authNativeRefreshTokenExpirationSeconds";

	// ------------------------------------------------------------------------- //
	// -------------------- Constructor(s) & Initialization -------------------- //
	// ------------------------------------------------------------------------- //

	/**
	 * Returns the configuration. At startup of the service it should be initialized with {@link
	 * #loadProperties(URL) loadProperties()}.
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
	 * Returns a date-time {@link String} representing the date and time that this version of the
	 * deployed external variable service was built.
	 *
	 * @return the build-time as a date-time {@link String}.
	 */
	public String getBuildTime() {
		if(get(BUILD_TIME) == null) return "";
		else return get(BUILD_TIME);
	}

	// ---------------------------------------------------------- //
	// -------------------- Getters: General -------------------- //
	// ---------------------------------------------------------- //

	/**
	 * Returns the configured Base URL for the DialogueBranch Web Service.
	 *
	 * @return the configured Base URL for the DialogueBranch Web Service.
	 */
	public String getBaseUrl() {
		if(get(BASE_URL) == null) return "";
		else return get(BASE_URL);
	}

	// ----------------------------------------------------------------- //
	// -------------------- Getters: Authentication -------------------- //
	// ----------------------------------------------------------------- //

	public String getAuthService() {
		if(get(AUTH_SERVICE) == null) return "";
		else return get(AUTH_SERVICE);
	}

	public String getKeycloakBaseUrl() {
		if(get(AUTH_KEYCLOAK_BASEURL) == null) return "";
		else return get(AUTH_KEYCLOAK_BASEURL);
	}

	public String getKeycloakRealm() {
		if(get(AUTH_KEYCLOAK_REALM) == null) return "";
		else return get(AUTH_KEYCLOAK_REALM);
	}

	public String getKeycloakClientId() {
		if(get(AUTH_KEYCLOAK_CLIENT_ID) == null) return "";
		else return get(AUTH_KEYCLOAK_CLIENT_ID);
	}

	public String getKeycloakClientSecret() {
		if(get(AUTH_KEYCLOAK_CLIENT_SECRET) == null) return "";
		else return get(AUTH_KEYCLOAK_CLIENT_SECRET);
	}

	public String getNativeServiceUser() {
		if(get(AUTH_NATIVE_SERVICE_USER) == null) return "";
		else return get(AUTH_NATIVE_SERVICE_USER);
	}

	public String getNativeServicePassword() {
		if(get(AUTH_NATIVE_SERVICE_PASSWORD) == null) return "";
		else return get(AUTH_NATIVE_SERVICE_PASSWORD);
	}

	/**
	 * Returns the secret key used for encoding/decoding JWT Access Tokens.
	 *
	 * @return the secret key used for encoding/decoding JWT Access Tokens.
	 */
	public String getJwtAccessTokenSecret() {
		if(get(AUTH_NATIVE_JWT_ACCESS_TOKEN_SECRET) == null) return "";
		else return get(AUTH_NATIVE_JWT_ACCESS_TOKEN_SECRET);
	}

	/**
	 * Returns the number of seconds a newly generated access token should be valid for under the
	 * native authentication service.
	 *
	 * @return the access token expiration time in seconds.
	 */
	public int getAccessTokenExpirationSeconds() {
		if (get(AUTH_NATIVE_ACCESS_TOKEN_EXPIRATION_SECONDS) == null) return 300;
		try {
			return Integer.parseInt(get(AUTH_NATIVE_ACCESS_TOKEN_EXPIRATION_SECONDS));
		} catch (NumberFormatException ex) {
			return 300;
		}
	}

	/**
	 * Returns the secret key used for encoding/decoding JWT Refresh Tokens.
	 *
	 * @return the secret key used for encoding/decoding JWT Refresh Tokens.
	 */
	public String getJwtRefreshTokenSecret() {
		if(get(AUTH_NATIVE_JWT_REFRESH_TOKEN_SECRET) == null) return "";
		else return get(AUTH_NATIVE_JWT_REFRESH_TOKEN_SECRET);
	}

	/**
	 * Returns the number of seconds a newly generated refresh token should be valid for under the
	 * native authentication service.
	 *
	 * @return the refresh token expiration time in seconds.
	 */
	public int getRefreshTokenExpirationSeconds() {
		if (get(AUTH_NATIVE_REFRESH_TOKEN_EXPIRATION_SECONDS) == null) return 1800;
		try {
			return Integer.parseInt(get(AUTH_NATIVE_REFRESH_TOKEN_EXPIRATION_SECONDS));
		} catch (NumberFormatException ex) {
			return 1800;
		}
	}

	@Override
	public String get(Object key) {
		// first try to find the key in the environment variables
		if (key instanceof String stringKey) {
			Map<String, String> env = System.getenv();
			for (String envKey : env.keySet()) {
				if (envKey.equalsIgnoreCase(stringKey)) {
					return env.get(envKey);
				}
			}
		}

		return super.get(key);
	}
}

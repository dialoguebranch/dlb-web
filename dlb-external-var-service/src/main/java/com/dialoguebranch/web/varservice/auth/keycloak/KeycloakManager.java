/*
 *
 *                Copyright (c) 2023-2025 Fruit Tree Labs (www.fruittreelabs.com)
 *
 *
 *     This material is part of the DialogueBranch Platform, and is covered by the MIT License
 *                                        as outlined below.
 *
 *                                            ----------
 *
 * Copyright (c) 2023-2024 Fruit Tree Labs (www.fruittreelabs.com)
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

package com.dialoguebranch.web.varservice.auth.keycloak;

import com.dialoguebranch.web.varservice.auth.AuthenticationInfo;
import com.dialoguebranch.web.varservice.Configuration;
import com.dialoguebranch.web.varservice.exception.UnauthorizedException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import nl.rrd.utils.AppComponents;
import org.slf4j.Logger;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.*;

/**
 *
 * @author Harm op den Akker
 */
public class KeycloakManager {

    /** Used for writing logging information */
    private final Logger logger = AppComponents.getLogger(getClass().getSimpleName());

    /** Used to access configuration parameters */
    private final Configuration config = Configuration.getInstance();

    /** Indicates whether the Keycloak manager has already been initialized. */
    private boolean initialized = false;

    /** A set of public RSA keys as obtained from the Keycloak instance, mapped by 'kid'. */
    private final Map<String,PublicKey> publicKeys;

    // -------------------------------------------------------- //
    // -------------------- Constructor(s) -------------------- //
    // -------------------------------------------------------- //

    public KeycloakManager() {
        this.publicKeys = new HashMap<>();

        //TODO: Start initializing from the moment this manager is created, but give it some
        // time for the Keycloak service to actually start up (and maybe attempt a few retries).
    }

    private void initialize() throws NoSuchAlgorithmException, InvalidKeySpecException {
        logger.info("Attempting to initialize KeycloakManager...");

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String keyCloakCertsUrl = config.getKeycloakBaseUrl();
        if(!keyCloakCertsUrl.endsWith("/")) keyCloakCertsUrl += "/";
        keyCloakCertsUrl += "realms/"
                + config.getKeycloakRealm()
                + "/protocol/openid-connect/certs";

        logger.info(" - Retrieving public Keycloak certificate data from: {} ...",
                keyCloakCertsUrl);

        HttpEntity<MultiValueMap<String,String>> entity = new HttpEntity<>(headers);
        ResponseEntity<KeycloakCertsResponse> response = restTemplate.exchange(
                keyCloakCertsUrl,
                HttpMethod.GET,
                entity,
                KeycloakCertsResponse.class);

        if(response.getStatusCode() == HttpStatus.OK) {
            logger.info(" - Response OK.");
            KeycloakCertsResponse keyCloakResponse = response.getBody();

            if(keyCloakResponse == null)
                throw new InvalidKeySpecException("Unable to get key information " +
                        "from response body.");

            for(KeycloakKey key : keyCloakResponse.getKeys()) {
                BigInteger modulus = new BigInteger(
                        1, Base64.getUrlDecoder().decode(key.getN()));
                BigInteger exponent = new BigInteger(
                        1,Base64.getUrlDecoder().decode(key.getE()));
                RSAPublicKeySpec rsaPublicKeySpec = new RSAPublicKeySpec(modulus, exponent);
                KeyFactory keyFactory = KeyFactory.getInstance(key.getKeyType());
                this.publicKeys.put(key.getKeyId(),keyFactory.generatePublic(rsaPublicKeySpec));
            }
            this.setInitialized();
            logger.info(" - KeycloakManager initialized successfully.");
        } else {
            logger.warn(" - Call to Keycloak token end-point failed.");
        }
    }

    public AuthenticationInfo validateToken(String token) throws UnauthorizedException {

        // Only on first time use, make sure the KeycloakManager is initialized (retrieved its
        // public keys).
        if(!this.isInitialized()) {
            try {
                this.initialize();
            } catch(NoSuchAlgorithmException noSuchAlgorithmException) {
                throw new UnauthorizedException(
                        "NoSuchAlgorithmException while validating token: "
                                + noSuchAlgorithmException.getMessage());
            } catch(InvalidKeySpecException invalidKeySpecException) {
                throw new UnauthorizedException(
                        "InvalidKeySpecException while validating token: "
                                + invalidKeySpecException.getMessage());
            }
        }

        // We need to extract the "key id" ("kid") from the header of the token.
        // Based on the "kid", we should use the correct, corresponding Public Key that we
        // obtained from the Keycloak service.

        // Split the JWT into its parts (header . payload . signature)
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new UnauthorizedException("Invalid JWT token.");
        }

        // Extract the keyID ("kid") from the header.
        String keyId;
        try {
            // Decode the header into a JSON String
            String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]));

            // Convert JSON to a map
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String,String> headerData = objectMapper.readValue(headerJson, Map.class);
            keyId = headerData.get("kid");
        } catch(JsonProcessingException e) {
            throw new UnauthorizedException("Unable to parse JWT header.");
        }

        final Claims claims = Jwts.parser()
                .verifyWith(this.publicKeys.get(keyId)) // Use the public key that matches this kid
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return new AuthenticationInfo(
                claims.get("preferred_username",String.class),
                claims.getIssuedAt(),
                claims.getExpiration());
    }

    // ----------------------------------------------------------- //
    // -------------------- Getters & Setters -------------------- //
    // ----------------------------------------------------------- //

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized() {
        this.initialized = true;
    }

}

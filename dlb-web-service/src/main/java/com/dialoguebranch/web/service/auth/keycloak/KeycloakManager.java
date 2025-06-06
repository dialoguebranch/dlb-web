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

package com.dialoguebranch.web.service.auth.keycloak;

import com.dialoguebranch.web.service.AuthDetails;
import com.dialoguebranch.web.service.Configuration;
import com.dialoguebranch.web.service.exception.UnauthorizedException;
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
import java.util.Base64;

public class KeycloakManager {

    /** Used for writing logging information */
    private final Logger logger = AppComponents.getLogger(getClass().getSimpleName());

    /** Used to access configuration parameters */
    private final Configuration config = Configuration.getInstance();

    /** Indicates whether the Keycloak manager has already been initialized. */
    private boolean initialized = false;

    /** The public part of the RSA key as obtained from the Keycloak instance. */
    private PublicKey publicKey;

    // -------------------------------------------------------- //
    // -------------------- Constructor(s) -------------------- //
    // -------------------------------------------------------- //

    public KeycloakManager() {

    }

    private void initialize() throws NoSuchAlgorithmException, InvalidKeySpecException {

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String keyCloakCertsUrl = config.getKeycloakBaseUrl();
        if(!keyCloakCertsUrl.endsWith("/")) keyCloakCertsUrl += "/";
        keyCloakCertsUrl += "realms/"
                + config.getKeycloakRealm()
                + "/protocol/openid-connect/certs";

        logger.info("Retrieving public Keycloak certificate data from: {}", keyCloakCertsUrl);

        HttpEntity<MultiValueMap<String,String>> entity = new HttpEntity<>(headers);
        ResponseEntity<KeycloakCertsResponse> response = restTemplate.exchange(
                keyCloakCertsUrl,
                HttpMethod.GET,
                entity,
                KeycloakCertsResponse.class);

        if(response.getStatusCode() == HttpStatus.OK) {
            logger.info("Successfully retrieved Keycloak certificates.");
            KeycloakCertsResponse keyCloakResponse = response.getBody();
            logger.info("Response: {}", keyCloakResponse);

            for(KeycloakKey key : keyCloakResponse.getKeys()) {
                if(key.getAlgorithm().equals("RS256")) {
                    BigInteger modulus = new BigInteger(1, Base64.getUrlDecoder().decode(key.getN()));
                    BigInteger exponent = new BigInteger(1,Base64.getUrlDecoder().decode(key.getE()));
                    RSAPublicKeySpec rsaPublicKeySpec = new RSAPublicKeySpec(modulus, exponent);
                    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                    this.publicKey = keyFactory.generatePublic(rsaPublicKeySpec);
                }
            }
            this.setInitialized();
        } else {
            logger.warn("Call to Keycloak token end-point failed.");
        }
    }

    public AuthDetails validateToken(String token) throws UnauthorizedException {

        // Only on first time use
        if(!this.isInitialized()) {
            try {
                this.initialize();
            } catch(NoSuchAlgorithmException nsae) {
                throw new UnauthorizedException(
                        "NoSuchAlgorithmException while validating token: "+nsae.getMessage());
            } catch(InvalidKeySpecException ikse) {
                throw new UnauthorizedException(
                        "InvalidKeySpecException while validating token: "+ikse.getMessage());
            }
        }

        final Claims claims = Jwts.parser()
                .verifyWith(this.publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return new AuthDetails(claims.get("preferred_username",String.class), claims.getIssuedAt(),
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

package com.dialoguebranch.web.service.execution;

import com.dialoguebranch.web.service.Configuration;
import com.dialoguebranch.web.service.controller.schema.LoginResultPayload;
import com.dialoguebranch.web.service.keycloak.KeycloakCertsResponse;
import nl.rrd.utils.AppComponents;
import org.slf4j.Logger;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class KeycloakManager {

    /** Used for writing logging information */
    private final Logger logger = AppComponents.getLogger(getClass().getSimpleName());

    private final Configuration config;

    public KeycloakManager(Configuration config) {
        this.config = config;
    }


    private void initialize() {

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
                HttpMethod.POST,
                entity,
                KeycloakCertsResponse.class);

        if(response.getStatusCode() == HttpStatus.OK) {
            logger.info("Successfully retrieved Keycloak certificates.");
            KeycloakCertsResponse keyCloakResponse = response.getBody();
            logger.info("Response: {}", keyCloakResponse);
        } else {
            logger.warn("Call to Keycloak token end-point failed.");
        }
    }
}

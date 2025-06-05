package com.dialoguebranch.web.service.keycloak;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.rrd.utils.json.JsonObject;

import java.util.ArrayList;

public class KeycloakCertsResponse extends JsonObject {

    @JsonProperty("keys")
    private ArrayList<KeycloakKey> keys;

    public KeycloakCertsResponse(ArrayList<KeycloakKey> keys) {
        this.keys = keys;
    }

    public ArrayList<KeycloakKey> getKeys() {
        return keys;
    }

    public void setKeys(ArrayList<KeycloakKey> keys) {
        this.keys = keys;
    }
}

package com.dialoguebranch.web.service.keycloak;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class KeycloakKey {

    @JsonProperty("kid")
    public String keyId;

    @JsonProperty("kty")
    public String keyType;

    @JsonProperty("alg")
    public String algorithm;

    @JsonProperty("use")
    public String use;

    @JsonProperty("x5c")
    public ArrayList<String> x5c;

    @JsonProperty("x5t")
    public String x5t;

    @JsonProperty("x5t#S256")
    public String x5tS256;

    @JsonProperty("n")
    public String n;

    @JsonProperty("e")
    public String e;


}

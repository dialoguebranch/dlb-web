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

    public KeycloakKey(String keyId, String keyType, String algorithm, String use, ArrayList<String> x5c, String x5t, String x5tS256, String n, String e) {
        this.keyId = keyId;
        this.keyType = keyType;
        this.algorithm = algorithm;
        this.use = use;
        this.x5c = x5c;
        this.x5t = x5t;
        this.x5tS256 = x5tS256;
        this.n = n;
        this.e = e;
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public String getKeyType() {
        return keyType;
    }

    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getUse() {
        return use;
    }

    public void setUse(String use) {
        this.use = use;
    }

    public ArrayList<String> getX5c() {
        return x5c;
    }

    public void setX5c(ArrayList<String> x5c) {
        this.x5c = x5c;
    }

    public String getX5t() {
        return x5t;
    }

    public void setX5t(String x5t) {
        this.x5t = x5t;
    }

    public String getX5tS256() {
        return x5tS256;
    }

    public void setX5tS256(String x5tS256) {
        this.x5tS256 = x5tS256;
    }

    public String getN() {
        return n;
    }

    public void setN(String n) {
        this.n = n;
    }

    public String getE() {
        return e;
    }

    public void setE(String e) {
        this.e = e;
    }

}

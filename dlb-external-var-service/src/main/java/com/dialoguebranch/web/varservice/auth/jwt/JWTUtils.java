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

package com.dialoguebranch.web.varservice.auth.jwt;

import com.dialoguebranch.web.varservice.auth.AuthenticationInfo;
import com.dialoguebranch.web.varservice.Configuration;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

public class JWTUtils {

    /** Used to access configuration parameters */
    private static final Configuration config = Configuration.getInstance();

    public static String generateAccessToken(String user) {
        return Jwts.builder()
                .expiration(new Date(System.currentTimeMillis()
                        + config.getAccessTokenExpirationSeconds() * 1000L))
                .issuedAt(new Date())
                .issuer(config.getBaseUrl())
                .subject(user)
                .claim("typ","Bearer") // Type of Token
                .claim("azp","dlb-external-var-service") // Authorized party
                .signWith(getAccessTokenSecret())
                .compact();
    }

    public static String generateRefreshToken(String user) {
        return Jwts.builder()
                .expiration(new Date(System.currentTimeMillis()
                        + config.getRefreshTokenExpirationSeconds() * 1000L))
                .issuedAt(new Date())
                .issuer(config.getBaseUrl())
                .audience().add(config.getBaseUrl()).and()
                .subject(user)
                .claim("typ","Refresh") // Type of Token
                .claim("azp","dlb-web-service") // Authorized party
                .signWith(getRefreshTokenSecret())
                .compact();
    }

    public static <T> T extractClaims(String token, Function<Claims, T> claimFunction) {
        Claims claims = Jwts.parser()
                .verifyWith(getAccessTokenSecret())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claimFunction.apply(claims);
    }

    public static AuthenticationInfo isAccessTokenValid(String token)
            throws JwtException {
        final Claims claims = Jwts.parser()
                .verifyWith(getAccessTokenSecret())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return new AuthenticationInfo(
                claims.getSubject(),
                claims.getIssuedAt(),
                claims.getExpiration());
    }

    public static AuthenticationInfo isRefreshTokenValid(String refreshToken)
            throws JwtException {
        final Claims claims = Jwts.parser()
                .verifyWith(getRefreshTokenSecret())
                .build()
                .parseSignedClaims(refreshToken)
                .getPayload();

        return new AuthenticationInfo(
                claims.getSubject(),
                claims.getIssuedAt(),
                claims.getExpiration());
    }

    /**
     * Obtains a {@link SecretKey} object used for encrypting and decrypting Access Tokens by
     * parsing the Base64 string value as defined in the configuration property
     * jwtAccessTokenSecret.
     *
     * @return the secret key as a {@link SecretKey} object.
     */
    private static SecretKey getAccessTokenSecret() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(Configuration.getInstance()
                .getJwtAccessTokenSecret()));
    }

    /**
     * Obtains a {@link SecretKey} object used for encrypting and decrypting Access Tokens by
     * parsing the Base64 string value as defined in the configuration property
     * jwtAccessTokenSecret.
     *
     * @return the secret key as a {@link SecretKey} object.
     */
    private static SecretKey getRefreshTokenSecret() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(Configuration.getInstance()
                .getJwtRefreshTokenSecret()));
    }

}

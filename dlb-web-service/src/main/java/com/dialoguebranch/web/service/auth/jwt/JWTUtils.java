/*
 *
 *                Copyright (c) 2023-2026 Fruit Tree Labs (www.fruittreelabs.com)
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

package com.dialoguebranch.web.service.auth.jwt;

import com.dialoguebranch.web.service.auth.AuthenticationInfo;
import com.dialoguebranch.web.service.Configuration;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

/**
 * Collection of static methods for creating and verifying JSON Web Tokens (JWTs).
 */
public class JWTUtils {

    private static final long EXPIRATION_TIME = 86400000; // 24 hours

    public static String generateAccessToken(AuthenticationInfo authenticationInfo) {
        return Jwts.builder()
                .subject(authenticationInfo.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .claim("roles", authenticationInfo.getCommaSeparatedRolesString())
                .signWith(getSecretKey())
                .compact();
    }

    public static <T> T extractClaims(String token, Function<Claims, T> claimFunction) {
        Claims claims = Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claimFunction.apply(claims);
    }

    public static AuthenticationInfo isAccessTokenValid(String token)
            throws JwtException {
        final Claims claims = Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        String rolesString = (String) claims.get("roles");
        String[] roles = rolesString.split(",");

        return new AuthenticationInfo(
                claims.getSubject(),
                roles,
                claims.getIssuedAt(),
                claims.getExpiration());
    }

    /**
     * Returns {@code true} if the given JWT is still valid.
     *
     * @param token the JSON Web Token for which to check the expiration time.
     * @return {@code true} if the token is still valid, false otherwise.
     */
    public static boolean isTokenExpired(String token) {
        return extractClaims(token, Claims::getExpiration).before(new Date());
    }

    /**
     * Gets the secret key by parsing the Base64 string in property jwtSecretKey in the
     * configuration.
     *
     * @return the secret key
     */
    private static SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(Configuration.getInstance()
                .getJwtSecretKey()));
    }

}

package com.dialoguebranch.web.service.auth.jwt;

import com.dialoguebranch.web.service.AuthDetails;
import com.dialoguebranch.web.service.Configuration;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

public class JWTUtils {

    private static final long EXPIRATION_TIME = 86400000; // 24 hours

    public static String generateToken(AuthDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getSubject())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
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

    public static AuthDetails isTokenValid(String token)
            throws JwtException {
        final Claims claims = Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return new AuthDetails(claims.getSubject(), claims.getIssuedAt(),
                claims.getExpiration());
    }

    public static boolean isTokenExpired(String token) {
        return extractClaims(token, Claims::getExpiration).before(new Date());
    }

    /**
     * Gets the secret key by parsing the Base64 string in property
     * jwtSecretKey in the configuration.
     *
     * @return the secret key
     */
    private static SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(Configuration.getInstance()
                .getJwtSecretKey()));
    }

}

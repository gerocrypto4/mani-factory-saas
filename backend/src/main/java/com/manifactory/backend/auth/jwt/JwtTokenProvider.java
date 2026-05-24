package com.manifactory.backend.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private final Key key;
    private final long validityMs;

    @Autowired
    public JwtTokenProvider(JwtProperties properties) {
        this(properties.getSecret(), properties.getExpirationMs());
    }

    public JwtTokenProvider(String secret, long validityMs) {
        if (secret == null || secret.isBlank() || "default-secret-please-change".equals(secret)) {
            // generate ephemeral key for dev if not set
            this.key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        } else {
            this.key = Keys.hmacShaKeyFor(secret.getBytes());
        }
        this.validityMs = validityMs;
    }

    public String createToken(String username, Long tenantId, String role) {
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID is required for token creation");
        }
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("Role is required for token creation");
        }
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("tenantId", tenantId);
        claims.put("role", role);
        Date now = new Date();
        Date exp = new Date(now.getTime() + validityMs);
        return Jwts.builder().setClaims(claims).setIssuedAt(now).setExpiration(exp).signWith(key).compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }
}

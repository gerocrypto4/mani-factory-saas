package com.manifactory.backend.auth.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret = "default-secret-please-change";
    private long expirationMs = 3600000L;
    private boolean allowInsecureDevKey = false;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public void setExpirationMs(long expirationMs) {
        this.expirationMs = expirationMs;
    }

    public boolean isAllowInsecureDevKey() {
        return allowInsecureDevKey;
    }

    public void setAllowInsecureDevKey(boolean allowInsecureDevKey) {
        this.allowInsecureDevKey = allowInsecureDevKey;
    }
}

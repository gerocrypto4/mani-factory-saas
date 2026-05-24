package com.manifactory.backend.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {

    private boolean enabled = true;
    private int maxRequests = 60;
    private int windowSeconds = 60;
    private int loginMaxRequests = 10;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getMaxRequests() {
        return maxRequests;
    }

    public void setMaxRequests(int maxRequests) {
        this.maxRequests = maxRequests;
    }

    public int getWindowSeconds() {
        return windowSeconds;
    }

    public void setWindowSeconds(int windowSeconds) {
        this.windowSeconds = windowSeconds;
    }

    public int getLoginMaxRequests() {
        return loginMaxRequests;
    }

    public void setLoginMaxRequests(int loginMaxRequests) {
        this.loginMaxRequests = loginMaxRequests;
    }
}

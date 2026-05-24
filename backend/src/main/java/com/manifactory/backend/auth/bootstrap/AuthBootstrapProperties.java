package com.manifactory.backend.auth.bootstrap;

import com.manifactory.backend.auth.entity.AppUserRole;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth.bootstrap")
public class AuthBootstrapProperties {

    private boolean enabled = true;
    private String username = "admin";
    private String password = "admin123";
    private Long tenantId = 1L;
    private AppUserRole role = AppUserRole.SUPERADMIN;
    private boolean allowDefaultCredentials = false;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public AppUserRole getRole() {
        return role;
    }

    public void setRole(AppUserRole role) {
        this.role = role;
    }

    public boolean isAllowDefaultCredentials() {
        return allowDefaultCredentials;
    }

    public void setAllowDefaultCredentials(boolean allowDefaultCredentials) {
        this.allowDefaultCredentials = allowDefaultCredentials;
    }
}

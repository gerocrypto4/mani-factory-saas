package com.manifactory.backend.auth.bootstrap;

import com.manifactory.backend.auth.entity.AppUser;
import com.manifactory.backend.auth.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AuthBootstrapInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AuthBootstrapInitializer.class);

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthBootstrapProperties bootstrapProperties;

    public AuthBootstrapInitializer(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            AuthBootstrapProperties bootstrapProperties) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.bootstrapProperties = bootstrapProperties;
    }

    @Override
    public void run(String... args) {
        if (!bootstrapProperties.isEnabled()) {
            return;
        }
        validateBootstrapSecurity();

        if (appUserRepository.existsByUsername(bootstrapProperties.getUsername())) {
            return;
        }

        AppUser user = AppUser.builder()
                .username(bootstrapProperties.getUsername())
                .passwordHash(passwordEncoder.encode(bootstrapProperties.getPassword()))
                .tenantId(bootstrapProperties.getTenantId())
                .role(bootstrapProperties.getRole())
                .active(true)
                .build();
        appUserRepository.save(user);

        log.warn("Bootstrap user created: username='{}', tenantId={}, role={}. Change password immediately.",
                user.getUsername(), user.getTenantId(), user.getRole());
    }

    private void validateBootstrapSecurity() {
        boolean defaultUsername = "admin".equalsIgnoreCase(bootstrapProperties.getUsername());
        boolean defaultPassword = "admin123".equals(bootstrapProperties.getPassword());
        if (defaultUsername && defaultPassword && !bootstrapProperties.isAllowDefaultCredentials()) {
            throw new IllegalStateException(
                    "Bootstrap credentials are insecure defaults. Configure AUTH_BOOTSTRAP_USERNAME/AUTH_BOOTSTRAP_PASSWORD.");
        }
    }
}

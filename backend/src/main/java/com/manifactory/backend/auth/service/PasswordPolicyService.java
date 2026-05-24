package com.manifactory.backend.auth.service;

import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class PasswordPolicyService {

    private static final Pattern DIGIT = Pattern.compile("\\d");

    public void validateOrThrow(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }
        if (!DIGIT.matcher(password).find()) {
            throw new IllegalArgumentException("Password must include at least one number");
        }
    }
}

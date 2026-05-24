package com.manifactory.backend.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

class RestAuthenticationEntryPointTest {

    @Test
    void shouldReturnJsonApiErrorForUnauthorizedRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/products");
        MockHttpServletResponse response = new MockHttpServletResponse();
        RestAuthenticationEntryPoint entryPoint = new RestAuthenticationEntryPoint();

        entryPoint.commence(request, response, new BadCredentialsException("Invalid credentials"));

        assertEquals(401, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        java.util.Map<String, Object> apiError = new ObjectMapper().readValue(response.getContentAsString(), new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>(){});
        assertEquals(401, ((Number) apiError.get("status")).intValue());
        assertEquals("BadCredentialsException", apiError.get("error"));
        assertEquals("Invalid credentials", apiError.get("message"));
        assertEquals("/api/v1/products", apiError.get("path"));
    }
}

package com.manifactory.backend.auth.jwt;

import com.manifactory.backend.security.TenantContextHolder;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String token = resolveToken(request);
            if (StringUtils.hasText(token) && tokenProvider.validateToken(token)) {
                Claims claims = tokenProvider.getClaims(token);
                String username = claims.getSubject();
                Object tenantObj = claims.get("tenantId");
                Long tenantId = null;
                if (tenantObj instanceof Integer) tenantId = ((Integer) tenantObj).longValue();
                else if (tenantObj instanceof Long) tenantId = (Long) tenantObj;

                // set tenant context
                if (tenantId != null) TenantContextHolder.setTenantId(tenantId);

                User principal = new User(username, "", java.util.Collections.emptyList());
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principal, token,
                        principal.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (Exception ex) {
            // ignore, security context stays empty
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContextHolder.clear();
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}

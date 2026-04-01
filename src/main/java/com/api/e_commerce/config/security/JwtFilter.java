package com.api.e_commerce.config.security;

import com.api.e_commerce.auth.TokenService;
import com.api.e_commerce.user.UserServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UserServiceImpl userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = extractTokenBearer(request);

        if (token == null || token.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String subject = tokenService.extractSubject(token);
            var user = userService.loadUserByUsername(subject);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

            //set the authentication in the security context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);
        } catch (AuthenticationException ex) {
            // In case of any exception during token verification, we can log the error and proceed without setting authentication
            // This will allow the request to be processed as an unauthenticated request, and the security configuration will handle it accordingly
            SecurityContextHolder.clearContext();
            log.warn("JWT inválido para {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        }
    }

    private String extractTokenBearer(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

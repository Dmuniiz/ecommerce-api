package com.api.e_commerce.config.security.filters;

import com.api.e_commerce.config.security.services.TokenProvider;
import com.api.e_commerce.user.UserServiceImpl;
import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final UserServiceImpl userService;
    private final TokenProvider tokenProvider;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
        }else{
            try{
                String token = extractTokenBearer(header);

                if(token != null && !token.isBlank()){
                    String username = tokenProvider.extractUsername(token);
                    var user = userService.loadUserByUsername(username); // Validate the token and load user details
                    var authorities = user.getAuthorities();

                    log.info("Authentication successful for user {}", username);
                    log.info("User authorities: {}", authorities);

                    var authentication = new UsernamePasswordAuthenticationToken(user, null, authorities);
                    authentication.setDetails(request);

                    SecurityContextHolder.getContext().setAuthentication(authentication);  //set the authentication in the security context
                }
            } catch (JWTVerificationException e) {
                log.warn("Received invalid auth token");
                SecurityContextHolder.clearContext();
                authenticationEntryPoint.commence(request, response, new org.springframework.security.authentication.BadCredentialsException("Invalid or expired token"));
                return;
            }
            filterChain.doFilter(request, response);
        }
    }

    private String extractTokenBearer(String bearerToken) {
        if(bearerToken != null && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }
        return null;
    }
}

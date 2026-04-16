package com.api.e_commerce.config.security;

import com.api.e_commerce.user.UserServiceImpl;
import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final UserServiceImpl userService;
    private final TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
        }else{
            try{
                String token = extractTokenBearer(header);

                if(token != null && !token.isBlank()){
                    String username = tokenService.extractSubject(token);
                    var user = userService.loadUserByUsername(username); // Validate the token and load user details

                    var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

                    SecurityContextHolder.getContext().setAuthentication(authentication);  //set the authentication in the security context
                }
            } catch (JWTVerificationException e) {
                log.warn("Received invalid auth token");
                throw new JWTVerificationException("Invalid or expired token: " + e.getMessage());
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

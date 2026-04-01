package com.api.e_commerce.config.security;

import com.api.e_commerce.auth.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        try{
            String token = extractTokenBearer(request);

            if(token != null){
                 var user = tokenService.verifyToken(token);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        null,
                        user.getAuthorities());

                //set the authentication in the security context
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }catch (Exception e){
            // In case of any exception during token verification, we can log the error and proceed without setting authentication
            // This will allow the request to be processed as an unauthenticated request, and the security configuration will handle it accordingly
            System.err.println("Error verifying token: " + e.getMessage());
        }

        filterChain.doFilter(request, response);

    }

    private String extractTokenBearer(HttpServletRequest request){
        String bearerToken = request.getHeader("Authorization");
        if(bearerToken != null){    
            return bearerToken.replace("Bearer ", "");
        }
        return null;
    }
}

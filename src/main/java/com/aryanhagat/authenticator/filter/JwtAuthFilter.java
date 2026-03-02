package com.aryanhagat.authenticator.filter;

import com.aryanhagat.authenticator.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    // OncePerRequestFilter guarantees this filter runs exactly once per request

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Step 1: Read the Authorization header
        String authHeader = request.getHeader("Authorization");

        // Step 2: If no header or doesn't start with "Bearer ", skip JWT auth
        // The request will still go through but won't be authenticated
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Step 3: Extract the token (remove "Bearer " prefix — 7 characters)
        String token = authHeader.substring(7);

        // Step 4: Validate the token
        if (!jwtService.isTokenValid(token)) {
            // Token is expired or tampered — reject the request
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid or expired token");
            return;
        }

        // Step 5: Extract email from token
        String email = jwtService.extractEmail(token);

        // Step 6: Tell Spring Security this request is authenticated
        // We create an Authentication object with the email as the principal
        // Third argument is authorities/roles — empty list for now (Phase 2 topic)
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(email, null, List.of());

        // Step 7: Store authentication in SecurityContext
        // This is how Spring Security knows the current user for this request
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Step 8: Pass the request to the next filter / controller
        filterChain.doFilter(request, response);
    }
}

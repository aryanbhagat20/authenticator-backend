package com.aryanhagat.authenticator.config;

import com.aryanhagat.authenticator.filter.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF — not needed for stateless JWT APIs
                .csrf(csrf -> csrf.disable())

                // Define which endpoints are public and which require auth
                .authorizeHttpRequests(auth -> auth
                        // These endpoints are public — no token needed
                        .requestMatchers("/auth/signup", "/auth/login", "/auth/login/2fa").permitAll()
                        // This endpoint is public — user needs to scan QR before they have a token
                        .requestMatchers("/2fa/qr").permitAll()
                        // Everything else requires a valid JWT
                        .anyRequest().authenticated()
                )

                // Tell Spring Security: don't create sessions
                // JWT is stateless — we never use server-side sessions
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Add our JWT filter BEFORE Spring's built-in username/password filter
                // This means JWT auth runs first on every request
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
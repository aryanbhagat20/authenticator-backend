package com.aryanhagat.authenticator.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()

                .info(new Info()
                        .title("Authenticator API")
                        .description(
                                "A secure authentication REST API featuring JWT tokens, " +
                                        "Two-Factor Authentication (2FA) via TOTP, refresh token rotation, " +
                                        "BCrypt password hashing, input validation, and rate limiting."
                        )
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Aryan Hagat")
                                .email("your.email@gmail.com")
                        )
                )

                // JWT Security Scheme
                // automatically send Authorization: Bearer <token>
                .addSecurityItem(new SecurityRequirement()
                        .addList("Bearer Authentication")
                )
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .name("Bearer Authentication")
                                        .description(
                                                "Enter your JWT token. " +
                                                        "Get it from POST /auth/login or POST /auth/refresh"
                                        )
                        )
                );
    }
}
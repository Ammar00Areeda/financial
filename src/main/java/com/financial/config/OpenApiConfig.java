package com.financial.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration class for OpenAPI (Swagger) documentation.
 * The YAML file will be automatically loaded by SpringDoc.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "Bearer Authentication";
        
        return new OpenAPI()
                .info(new Info()
                        .title("Financial Management API")
                        .description("""
                                # Financial Management REST API
                                
                                A comprehensive REST API for managing personal finances, including:
                                - **User Authentication**: JWT-based secure authentication
                                - **Account Management**: Manage multiple financial accounts (bank accounts, wallets, savings, etc.)
                                - **Transaction Tracking**: Record and categorize income, expenses, and transfers
                                - **Category Management**: Organize transactions with custom categories
                                - **Loan Tracking**: Track money lent to and borrowed from others
                                - **Recurring Expenses**: Manage subscription and recurring payments
                                - **Dashboard & Reports**: View financial summaries and analytics
                                
                                ## Authentication
                                Most endpoints require authentication using JWT tokens. To authenticate:
                                1. Register a new user via `/api/auth/register` (no auth required)
                                2. Login via `/api/auth/login` to receive a JWT token
                                3. Include the token in the Authorization header: `Bearer <your-token>`
                                
                                ## Default Test Users
                                - **Admin**: username=`admin`, password=`admin123`
                                - **User**: username=`user`, password=`user123`
                                
                                ## Authorization Levels
                                - **Public**: `/api/auth/**` endpoints are accessible without authentication
                                - **User**: Most endpoints require authentication
                                - **Admin**: `/api/users/**` endpoints require ADMIN role
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Financial API Team")
                                .email("support@financial.com")
                                .url("https://financial.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080/api")
                                .description("Local Development Server"),
                        new Server()
                                .url("http://localhost:8081/api")
                                .description("Docker Development Server"),
                        new Server()
                                .url("https://api.financial.com")
                                .description("Production Server")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("""
                                                JWT authentication token. 
                                                
                                                To obtain a token:
                                                1. Send POST request to `/api/auth/login` with username and password
                                                2. Copy the token from the response
                                                3. Click 'Authorize' button above and enter: Bearer <your-token>
                                                
                                                Token format: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
                                                """)
                        )
                );
    }
}

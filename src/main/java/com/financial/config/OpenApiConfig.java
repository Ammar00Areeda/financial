package com.financial.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
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
        return new OpenAPI()
                .info(new Info()
                        .title("Financial Management API")
                        .description("A comprehensive REST API for financial management system including user management, account operations, and transaction processing.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Financial Team")
                                .email("support@financial.com")
                                .url("https://financial.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8081/api")
                                .description("Development Server"),
                        new Server()
                                .url("https://api.financial.com/v1")
                                .description("Production Server")
                ));
    }
}

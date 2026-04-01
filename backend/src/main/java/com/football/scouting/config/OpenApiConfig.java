package com.football.scouting.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI footballScoutingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Football Scouting API")
                        .description("API de gestion de scouting de joueurs de football")
                        .version("v1")
                        .contact(new Contact()
                                .name("Princy")
                                .email("rafamatanantsoa10@gmail.com"))
                        .license(new License()
                                .name("Usage interne")));
    }
}
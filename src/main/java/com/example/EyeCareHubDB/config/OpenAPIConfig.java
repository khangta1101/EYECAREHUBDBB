package com.example.EyeCareHubDB.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        Server localServer = new Server();
        localServer.setUrl("http://localhost:8080");
        localServer.setDescription("Server Localhost");

        Server productionServer = new Server();
        // Nhớ thay đường link này thành đường link thật của Railway của bạn
        productionServer.setUrl("https://eyecarehubdbb-production.up.railway.app");
        productionServer.setDescription("Server Production (Railway)");

        return new OpenAPI().servers(List.of(localServer, productionServer));
    }
}

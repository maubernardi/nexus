package it.nexus.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import it.nexus.config.security.MockHeaderAuthenticationFilter;

@Configuration
public class OpenApiConfig {

    private static final String BEARER = "bearer-jwt";
    private static final String MOCK_HEADER = "mock-user";

    @Bean
    OpenAPI nexusOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("NEXUS API")
                        .description("API per Area Lavoro e Call Center Sociale")
                        .version("v1"))
                .components(new Components()
                        .addSecuritySchemes(BEARER, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT"))
                        .addSecuritySchemes(MOCK_HEADER, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.HEADER)
                                .name(MockHeaderAuthenticationFilter.HEADER)
                                .description("Solo profilo security-mock (sviluppo locale)")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER))
                .addSecurityItem(new SecurityRequirement().addList(MOCK_HEADER));
    }
}

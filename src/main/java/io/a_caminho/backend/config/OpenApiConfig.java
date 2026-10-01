package io.a_caminho.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_SECURITY_SCHEME = "bearerAuth";
    public static final String COOKIE_SECURITY_SCHEME = "cookieAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("A Caminho API")
                        .description("Documentação da API REST da plataforma A Caminho — Sistema de Gestão do Transporte Universitário Intermunicipal.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipe A Caminho")
                                .email("contato@a-caminho.io"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SECURITY_SCHEME,
                                new SecurityScheme()
                                        .name(BEARER_SECURITY_SCHEME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Insira o Access Token JWT obtido no endpoint de login."))
                        .addSecuritySchemes(COOKIE_SECURITY_SCHEME,
                                new SecurityScheme()
                                        .name("refresh_token")
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.COOKIE)
                                        .description("Cookie HttpOnly que contém o Refresh Token para rotação e renovação de acesso.")));
    }
}

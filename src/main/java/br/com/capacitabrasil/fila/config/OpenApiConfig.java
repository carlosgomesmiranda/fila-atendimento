package br.com.capacitabrasil.fila.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI filaAtendimentoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API - Fila de Atendimento")
                        .description("""
                                MVP de gerenciamento de filas de atendimento presencial.
                                Emissao de senhas, chamada, atendimento, historico e estatisticas.
                                Perfis: USUARIO, ATENDENTE e ADMINISTRADOR.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Programa Residencia em TIC - Capacita Brasil / iRede")
                                .email("contato@capacitabrasil.example"))
                        .license(new License().name("MIT")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME,
                        new SecurityScheme()
                                .name(SECURITY_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}

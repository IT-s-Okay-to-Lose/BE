package com.example.iotl.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@OpenAPIDefinition(
        info = @Info(title = "IOTL API", version = "v1", description = "투자 대시보드 백엔드 API 명세서"),
        servers = {
                @Server(url = "https://iotl.store", description = "Production Server"),
                @Server(url = "http://localhost:8080", description = "Local Server")
        }
)
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("IOTL API")
                        .description("투자 대시보드 백엔드 API 명세서입니다.")
                        .version("v1")
                        .contact(new Contact()
                                .name("IOTL 개발팀")
                                .email("iotl@example.com")
                                .url("https://github.com/IT-s-Okay-to-Lose/BE"))
                        .license(new License().name("MIT").url("https://opensource.org/licenses/MIT"))
                )
                .servers(List.of(
                        new io.swagger.v3.oas.models.servers.Server()
                                .url("https://iotl.store")
                                .description("Production Server"),
                        new io.swagger.v3.oas.models.servers.Server()
                                .url("http://localhost:8080")
                                .description("Local Server")
                ));
    }
    // 옵션: 그룹 나누고 싶을 경우
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("v1-public")
                .pathsToMatch("/api/**")
                .build();
    }
}
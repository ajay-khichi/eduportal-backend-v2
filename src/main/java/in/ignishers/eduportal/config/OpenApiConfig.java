package in.ignishers.eduportal.config;

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

    private static final String BEARER_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI eduportalOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Eduportal Backend v2 API")
                        .version("2.0.0")
                        .description("Academic ERP & Multi-Factor Smart Attendance Engine (5s Rolling QR, GPS Haversine Geofencing, Face Biometrics)")
                        .contact(new Contact().name("Ignishers Eduportal Team")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME_NAME, new SecurityScheme()
                                .name(BEARER_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}

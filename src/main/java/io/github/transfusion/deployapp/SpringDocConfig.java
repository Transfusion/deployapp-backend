package io.github.transfusion.deployapp;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfig {

    /**
     * <a href="https://springdoc.org/#can-i-customize-openapi-object-programmatically">...</a>
     * @return OpenAPI object with application-specific details
     */
    @Bean
    public OpenAPI deployAppOpenAPI() {
        return new OpenAPI().info(new Info().title("DeployApp API").description("DeployApp Backend").version("v0.0.1"));
    }
}

package io.github.transfusion.deployapp;


import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@EnableWebMvc
public class CorsConfig {
    @Autowired
    private Environment env;

    @Value("${custom_cors.origins}")
    private List<String> corsOrigins;

    // /actuator/** CORS configuration is done in the application properties
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NotNull CorsRegistry registry) {
//                List<String> urls = env.getProperty("custom_cors.origins", List.class);
                CorsRegistration reg = registry.addMapping("/api/**");
                reg.allowedOrigins(corsOrigins.toArray(new String[0]));
                reg.allowedHeaders("*").allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS").allowCredentials(true);
            }
        };
    }

}

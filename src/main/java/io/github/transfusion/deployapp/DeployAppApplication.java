package io.github.transfusion.deployapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.request.RequestContextListener;

@SpringBootApplication
public class DeployAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeployAppApplication.class, args);
    }

    // https://stackoverflow.com/questions/29235041/get-the-current-session-with-spring-session
    // used in CustomOAuth2UserService to access cookies while loading the user
    @Bean
    public RequestContextListener requestContextListener() {
        return new RequestContextListener();
    }

}

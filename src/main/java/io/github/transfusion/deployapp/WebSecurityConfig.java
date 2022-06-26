package io.github.transfusion.deployapp;

import io.github.transfusion.deployapp.auth.AuthenticationEntryPoint;
import io.github.transfusion.deployapp.auth.CustomAuthenticationProvider;
import io.github.transfusion.deployapp.auth.CustomOAuth2UserService;
import io.github.transfusion.deployapp.auth.CustomUserDetailsService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.JdbcOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.sql.DataSource;
import java.util.List;

@Configuration
public class WebSecurityConfig {

    @Autowired
    private HttpCookieOAuth2AuthorizationRequestRepository cookieOAuth2AuthorizationRequestRepository;

    @Autowired
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Autowired
    private OAuth2AuthorizedClientService oAuth2AuthorizedClientService;

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Bean
    CustomAuthenticationProvider customAuthenticationProvider() {
        return new CustomAuthenticationProvider();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http.authorizeHttpRequests((authz) -> authz.anyRequest().authenticated()).httpBasic(withDefaults());
        http.cors()
                .and()
                .csrf()
                .disable()
                .formLogin()
                .disable()
//                .authenticationProvider(customAuthenticationProvider())
                .exceptionHandling()
                .authenticationEntryPoint(new AuthenticationEntryPoint())
                .and()
                // authorize all requests to the /oauth2 endpoint
                .authorizeRequests()
                .antMatchers("/oauth2/**", "/api/*/user/profile")
                .permitAll()
                .anyRequest().authenticated()
                .and()
                .sessionManagement(config -> {
                    config.sessionCreationPolicy(SessionCreationPolicy.ALWAYS);
                })

                .oauth2Login().authorizedClientService(oAuth2AuthorizedClientService)
                .authorizationEndpoint()
                .baseUri("/oauth2/authorize")
                .authorizationRequestRepository(cookieOAuth2AuthorizationRequestRepository)
                .and().userInfoEndpoint().userService(customOAuth2UserService)
                .and().successHandler(oAuth2AuthenticationSuccessHandler);
//                .redirectionEndpoint()
//                .baseUri("/oauth2/callback");
        return http.build();
    }

//    @Bean
//    public WebSecurityCustomizer webSecurityCustomizer() {
//        return (web) -> web.ignoring().antMatchers("/login/**", "/ignore2");
//    }
}

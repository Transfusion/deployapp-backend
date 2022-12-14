package io.github.transfusion.deployapp;

import io.github.transfusion.deployapp.auth.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.JdbcOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.sql.DataSource;
import java.util.List;
import java.util.function.Consumer;

@Configuration
public class WebSecurityConfig {

    @Autowired
    private HttpCookieOAuth2AuthorizationRequestRepository cookieOAuth2AuthorizationRequestRepository;

    @Autowired
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Autowired
    private OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    @Autowired
    private OAuth2AuthorizedClientService oAuth2AuthorizedClientService;

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    /* @Autowired
    private ClientRegistrationRepository clientRegistrationRepository; */

    @Bean
    CustomAuthenticationProvider customAuthenticationProvider() {
        return new CustomAuthenticationProvider();
    }

    @Value("${custom_cors.origins}")
    private List<String> corsOrigins;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http.authorizeHttpRequests((authz) -> authz.anyRequest().authenticated()).httpBasic(withDefaults());
        http.cors(Customizer.withDefaults())
//                .and()
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
                .antMatchers("/oauth2/**", "/api-docs/**", "/api/logout", "/api/*/user/profile", "/api/*/credentials/**", "/api/*/utility/public/**", "/microservice-api/**")
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
                .and().successHandler(oAuth2AuthenticationSuccessHandler)
                .failureHandler(oAuth2AuthenticationFailureHandler)

                .and().logout().logoutUrl("/api/logout")
                // Needed specifically for the /api/logout endpoint...
                .addLogoutHandler((request, response, authentication) -> {
                    String reqOrigin = request.getHeader("Origin");
                    if (corsOrigins.contains(reqOrigin)) {
                        response.setHeader("Access-Control-Allow-Origin", reqOrigin);
                        response.setHeader("Access-Control-Allow-Credentials", String.valueOf(true));
                        response.setHeader("Vary", "Origin");
                    }
                })
                .logoutSuccessHandler((new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK)));
//                .redirectionEndpoint()
//                .baseUri("/oauth2/callback");
        return http.build();
    }

    /* private OAuth2AuthorizationRequestResolver authorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository) {

        DefaultOAuth2AuthorizationRequestResolver authorizationRequestResolver =
                new DefaultOAuth2AuthorizationRequestResolver(
                        clientRegistrationRepository, "/oauth2/authorize");
        authorizationRequestResolver.setAuthorizationRequestCustomizer(
                authorizationRequestCustomizer());

        return authorizationRequestResolver;
    }

    private Consumer<OAuth2AuthorizationRequest.Builder> authorizationRequestCustomizer() {

        return customizer -> {
            SecurityContext context = SecurityContextHolder.getContext();
            Authentication authentication = context.getAuthentication();
            if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
                customizer
                        .additionalParameters(params -> params.put("_custom_original_user_id", ((CustomUserPrincipal) authentication.getPrincipal()).getId().toString()));
            }
        };
    }*/


//    @Bean
//    public WebSecurityCustomizer webSecurityCustomizer() {
//        return (web) -> web.ignoring().antMatchers("/login/**", "/ignore2");
//    }
}

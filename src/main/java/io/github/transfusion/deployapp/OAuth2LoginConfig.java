package io.github.transfusion.deployapp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.client.JdbcOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import javax.sql.DataSource;

@Configuration
public class OAuth2LoginConfig {

    @Bean
    OAuth2AuthorizedClientService authorizedClientService(DataSource dataSource, ClientRegistrationRepository clientRegRepo) {
        System.out.println(" - JDBC: creating JdbcOAuth2AuthorizedClientService");
        JdbcTemplate jdbcOps = new JdbcTemplate(dataSource);
        JdbcOAuth2AuthorizedClientService clientService = new JdbcOAuth2AuthorizedClientService(jdbcOps, clientRegRepo);
        clientService.setAuthorizedClientParametersMapper(new PostgreSqlOAuth2AuthorizedClientParametersMapper());
        clientService.setAuthorizedClientRowMapper(new UtcOAuth2AuthorizedClientRowMapper(clientRegRepo));
        return clientService;
    }

}

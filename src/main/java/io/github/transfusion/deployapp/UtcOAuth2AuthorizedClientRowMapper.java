package io.github.transfusion.deployapp;

import org.springframework.security.oauth2.client.JdbcOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * <a href="https://github.com/spring-projects/spring-security/issues/8539">https://github.com/spring-projects/spring-security/issues/8539</a>
 */
public class UtcOAuth2AuthorizedClientRowMapper
        extends JdbcOAuth2AuthorizedClientService.OAuth2AuthorizedClientRowMapper {

    public UtcOAuth2AuthorizedClientRowMapper(ClientRegistrationRepository clientRegistrationRepository) {
        super(clientRegistrationRepository);
    }

    @Override
    public OAuth2AuthorizedClient mapRow(ResultSet rs, int i) throws SQLException {
        ResultSet rsWrapper = (ResultSet) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{ResultSet.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("getTimestamp") && args.length == 1) {
                        // getting as UTC stamp!
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
                        Object theOnlyArg = args[0];
                        if (theOnlyArg.getClass() == String.class) {
                            return rs.getTimestamp((String) theOnlyArg, cal);
                        } else if (theOnlyArg.getClass() == Integer.class) {
                            return rs.getTimestamp((Integer) theOnlyArg, cal);
                        }
                    }
                    return method.invoke(rs, args);
                });
        return super.mapRow(rsWrapper, i);
    }
}

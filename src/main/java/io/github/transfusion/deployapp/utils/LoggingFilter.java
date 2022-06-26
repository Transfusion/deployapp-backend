package io.github.transfusion.deployapp.utils;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import javax.servlet.http.HttpServletRequest;

@Configuration
public class LoggingFilter {
    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter logFilter = new CommonsRequestLoggingFilter() {

            @Override
            protected boolean shouldLog(HttpServletRequest request) {
                return true;
            }


            @Override
            protected void beforeRequest(HttpServletRequest request, String message) {
                // Do nothing if you need logging payload.
                // As, Before the Request, the payload is not read from the input-stream, yet.
            }


            @Override
            protected void afterRequest(HttpServletRequest request, String message) {
                logger.info(message); // Or log to a file here, as OP asks.
            }


            @Override
            protected @NonNull String createMessage(HttpServletRequest request, @NonNull String prefix, @NonNull String suffix) {
                // Output: [PUT][/api/my-entity], user:[my-loging], payload was:[{ "id": 33, "value": 777.00}]
                StringBuilder msg = new StringBuilder().append(prefix).append("[").append(request.getMethod()).append("]").append("[").append(request.getRequestURI()).append("]");

                String user = request.getRemoteUser();
                msg.append(", user:[").append(null == user ? "" : user).append("]");

                String payload = getMessagePayload(request);
                if (payload != null) {
                    // It's not null on After event. As, on Before event, the Input stream was not read, yet.
                    msg.append(", payload was:[").append(payload.replace("\n", "")).append("]");  // Remove /n to be compliant with elastic search readers.
                }

                msg.append(suffix);
                return msg.toString();
            }
        };
        logFilter.setBeforeMessagePrefix("Incoming REST call: -->>>[");
        logFilter.setBeforeMessageSuffix("]...");
        logFilter.setAfterMessagePrefix("REST call processed: -<<<[");
        logFilter.setAfterMessageSuffix("]");
        logFilter.setIncludePayload(true);
        logFilter.setMaxPayloadLength(64000);
        return logFilter;
    }
}
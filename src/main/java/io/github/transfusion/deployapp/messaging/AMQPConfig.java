package io.github.transfusion.deployapp.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AMQPConfig {
    public static final String EXCHANGE_NAME = "deployapp-exchange";
    public static final String INTEGRATION_EVENTS_QUEUE_NAME = "integration_events";

    @Bean
    public TopicExchange deployAppExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    public static String INTEGRATION_EVENTS_ROUTING_KEY = "ievent";

    @Bean
    public Queue integrationEventsQueue() {
        return new Queue(INTEGRATION_EVENTS_QUEUE_NAME);
    }

    @Bean
    public Binding declareIntegrationEventsBinding() {
        return BindingBuilder.bind(integrationEventsQueue())
                .to(deployAppExchange()).with(INTEGRATION_EVENTS_ROUTING_KEY);
    }


}

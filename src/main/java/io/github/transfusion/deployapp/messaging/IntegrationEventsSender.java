package io.github.transfusion.deployapp.messaging;

import io.github.transfusion.deployapp.dto.response.FtpCredentialDTO;
import io.github.transfusion.deployapp.dto.response.S3CredentialDTO;
import io.github.transfusion.deployapp.dto.response.StorageCredentialDTO;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static io.github.transfusion.deployapp.messaging.AMQPConfig.EXCHANGE_NAME;
import static io.github.transfusion.deployapp.messaging.AMQPConfig.INTEGRATION_EVENTS_ROUTING_KEY;

@Service
public class IntegrationEventsSender {

    @Autowired
    private RabbitTemplate template;

//    @Autowired
//    private Queue queue;

    public void send(Object o) {
        template.convertAndSend(EXCHANGE_NAME, INTEGRATION_EVENTS_ROUTING_KEY, o
        );
    }
//    public void send(StorageCredentialDTO storageCredentialDTO) {
//        if (storageCredentialDTO instanceof S3CredentialDTO) {
//            template.convertAndSend((S3CredentialDTO) storageCredentialDTO);
//        } else if (storageCredentialDTO instanceof FtpCredentialDTO) {
//            template.convertAndSend((FtpCredentialDTO) storageCredentialDTO);
//        } else {
//            throw new IllegalArgumentException("Given storageCredentialDTO is of unrecognized type");
//        }
//    }
}

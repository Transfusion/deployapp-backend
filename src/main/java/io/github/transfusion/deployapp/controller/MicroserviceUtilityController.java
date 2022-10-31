package io.github.transfusion.deployapp.controller;

import io.github.transfusion.deployapp.dto.internal.TestMessage;
import io.github.transfusion.deployapp.messaging.IntegrationEventsSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/microservice-api/v1/utility")
public class MicroserviceUtilityController {

    @Autowired
    private IntegrationEventsSender sender;

    @PostMapping("/amqp-test")
    public ResponseEntity<Void> amqpTest() {
        TestMessage msg = new TestMessage("Hello World");
        sender.send(msg);
        return null;
    }
}

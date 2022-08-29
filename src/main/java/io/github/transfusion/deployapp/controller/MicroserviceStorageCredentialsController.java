package io.github.transfusion.deployapp.controller;

import io.github.transfusion.deployapp.dto.response.StorageCredentialDTO;
import io.github.transfusion.deployapp.services.StorageCredentialsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;


@RestController
@RequestMapping("/microservice-api/v1/credentials")
public class MicroserviceStorageCredentialsController {


    Logger logger = LoggerFactory.getLogger(MicroserviceStorageCredentialsController.class);

    @Autowired
    private StorageCredentialsService storageCredentialsService;

    @GetMapping("/{id}")
    public ResponseEntity<StorageCredentialDTO> getCredentialById(@PathVariable("id") UUID id, HttpServletRequest request) {
        return storageCredentialsService.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}

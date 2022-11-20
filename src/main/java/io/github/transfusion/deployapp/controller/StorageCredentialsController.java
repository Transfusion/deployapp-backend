package io.github.transfusion.deployapp.controller;

import io.github.transfusion.deployapp.dto.internal.DeleteStorageCredentialEvent;
import io.github.transfusion.deployapp.dto.request.CreateFtpCredentialRequest;
import io.github.transfusion.deployapp.dto.request.CreateS3CredentialRequest;
import io.github.transfusion.deployapp.dto.request.UpdateFtpCredentialRequest;
import io.github.transfusion.deployapp.dto.request.UpdateS3CredentialRequest;
import io.github.transfusion.deployapp.dto.response.*;
import io.github.transfusion.deployapp.messaging.IntegrationEventsSender;
import io.github.transfusion.deployapp.services.StorageCredentialsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/credentials")
public class StorageCredentialsController {

    Logger logger = LoggerFactory.getLogger(StorageCredentialsController.class);

    @Autowired
    private StorageCredentialsService storageCredentialsService;

    @Autowired
    private HttpSession session;

    @GetMapping()
    public ResponseEntity<Page<StorageCredentialDTO>> list(
            // fuzzy match
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(required = false) List<String> types,
            Pageable page
    ) {
        Page<StorageCredentialDTO> resultPage = storageCredentialsService.findPaginated(name, types, page);
//        if (p.getPageNumber() > resultPage.getTotalPages()) {
//            throw new IllegalArgumentException(String.format("Trying to access page %d out of %d", page, resultPage.getTotalPages()));
//        }

        return new ResponseEntity<>(resultPage, HttpStatus.OK);
    }

//    @GetMapping("S3")
//    public ResponseEntity<List<S3CredentialsDTO>> getAllS3Creds() {
//
//    }

    @Autowired
    private IntegrationEventsSender integrationEventsSender;

    @PostMapping("S3")
    public ResponseEntity<S3CreateResultDTO> createS3Credential(@RequestBody CreateS3CredentialRequest request) throws IOException {
        S3CreateResultDTO result = storageCredentialsService.createS3Credential(request);
        if (result.getSuccess()) integrationEventsSender.send(storageCredentialsService.findById(result.getId()).get());
        return new ResponseEntity<>(result, result.getSuccess() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST);
    }

    @PutMapping("S3/{id}")
    public ResponseEntity<S3UpdateResultDTO> updateS3Credential(@PathVariable("id") UUID id,
                                                                @RequestBody UpdateS3CredentialRequest request) {
        S3UpdateResultDTO result = storageCredentialsService.updateS3Credential(id, request);
        if (result.getSuccess()) integrationEventsSender.send(storageCredentialsService.findById(result.getId()).get());
        return new ResponseEntity<>(result, result.getSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping("S3/{id}")
    public ResponseEntity<Void> deleteS3Credential(@PathVariable("id") UUID id) {
        storageCredentialsService.deleteStorageCredential(id);
        integrationEventsSender.send(new DeleteStorageCredentialEvent(id));
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @PostMapping("FTP")
    public ResponseEntity<FtpCreateResultDTO> createFtpCredential(@RequestBody CreateFtpCredentialRequest request) throws IOException {
        FtpCreateResultDTO result = storageCredentialsService.createFtpCredential(request);
        if (result.getSuccess()) integrationEventsSender.send(storageCredentialsService.findById(result.getId()).get());
        return new ResponseEntity<>(result, result.getSuccess() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST);
    }

    @PutMapping("FTP/{id}")
    public ResponseEntity<FtpUpdateResultDTO> updateFtpCredential(@PathVariable("id") UUID id,
                                                                  @RequestBody UpdateFtpCredentialRequest request) {
        FtpUpdateResultDTO result = storageCredentialsService.updateFtpCredential(id, request);
        if (result.getSuccess()) integrationEventsSender.send(storageCredentialsService.findById(result.getId()).get());
        return new ResponseEntity<>(result, result.getSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping("FTP/{id}")
    public ResponseEntity<Void> deleteFtpCredential(@PathVariable("id") UUID id) {
        storageCredentialsService.deleteStorageCredential(id);
        integrationEventsSender.send(new DeleteStorageCredentialEvent(id));
        return new ResponseEntity<>(HttpStatus.OK);
    }
}

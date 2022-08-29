package io.github.transfusion.deployapp.controller;

import io.github.transfusion.deployapp.dto.request.CreateS3CredentialRequest;
import io.github.transfusion.deployapp.dto.response.S3CreateResultDTO;
import io.github.transfusion.deployapp.dto.response.StorageCredentialDTO;
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

    @PostMapping("S3")
    public ResponseEntity<S3CreateResultDTO> createS3Credential(@RequestBody CreateS3CredentialRequest request) throws IOException {
        S3CreateResultDTO result = storageCredentialsService.createS3Credential(request);
        return new ResponseEntity<>(result, result.getSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }
}

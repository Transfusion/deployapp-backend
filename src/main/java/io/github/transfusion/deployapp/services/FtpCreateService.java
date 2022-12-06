package io.github.transfusion.deployapp.services;

import io.github.transfusion.deployapp.db.entities.FtpCredential;
import io.github.transfusion.deployapp.db.entities.User;
import io.github.transfusion.deployapp.db.repositories.StorageCredentialRepository;
import io.github.transfusion.deployapp.dto.internal.FtpTestResult;
import io.github.transfusion.deployapp.dto.request.CreateFtpCredentialRequest;
import io.github.transfusion.deployapp.dto.response.FtpCreateResultDTO;
import io.github.transfusion.deployapp.dto.response.FtpCredentialDTO;
import io.github.transfusion.deployapp.dto.response.StorageCredentialDTO;
import io.github.transfusion.deployapp.mappers.StorageCredentialMapper;
import io.github.transfusion.deployapp.session.SessionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class FtpCreateService {

    @Autowired
    private FtpTesterService ftpTesterService;

    @Autowired
    private StorageCredentialRepository storageCredentialRepository;

    @Autowired
    private StorageCredentialMapper storageCredentialMapper;

    @Autowired
    private SessionData sessionData;

    public FtpCreateResultDTO createFtpCredential(User user, CreateFtpCredentialRequest request) {
        FtpCredential ftp = new FtpCredential();

        ftp.setUser(user);
        storageCredentialMapper.updateFtpCredentialFromCreateRequest(request, ftp);
            /*ftp.setUsername(request.getUsername());
            ftp.setPassword(request.getPassword());
            ftp.setServer(request.getServer());
            ftp.setBaseUrl(request.getBaseUrl());
            ftp.setDirectory(request.getDirectory());*/

        Instant now = Instant.now();
        ftp.setCreatedOn(now);
        ftp.setCheckedOn(now);

        FtpTestResult ftpTestResult = ftpTesterService.test(ftp).join();
        boolean overallSuccess = ftpTestResult.getTestConnectionSuccess() && ftpTestResult.getTestWriteFolderSuccess()
                && ftpTestResult.getTestPublicAccessSuccess();

        if (overallSuccess) {
            UUID savedId = storageCredentialRepository.save(ftp).getId();
            return new FtpCreateResultDTO(overallSuccess, savedId, ftpTestResult, (FtpCredentialDTO) findById(savedId).get());
        } else {
            return new FtpCreateResultDTO(overallSuccess, null, ftpTestResult, null);
        }
    }

    public FtpCreateResultDTO createFtpCredentialAnonymous(User user, CreateFtpCredentialRequest request) {
        FtpCreateResultDTO ftpCreateResultDTO = createFtpCredential(user, request);
        if (ftpCreateResultDTO.getSuccess())
            sessionData.getAnonymousCredentials().add(ftpCreateResultDTO.getId());
        return ftpCreateResultDTO;
    }

    private Optional<StorageCredentialDTO> findById(UUID id) {
        return storageCredentialRepository.findById(id).map(storageCredentialMapper::toDTO);
    }
}

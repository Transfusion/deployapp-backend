package io.github.transfusion.deployapp.services;

import io.github.transfusion.deployapp.db.entities.S3Credential;
import io.github.transfusion.deployapp.db.entities.User;
import io.github.transfusion.deployapp.db.repositories.StorageCredentialRepository;
import io.github.transfusion.deployapp.dto.internal.S3TestResult;
import io.github.transfusion.deployapp.dto.request.CreateS3CredentialRequest;
import io.github.transfusion.deployapp.dto.response.S3CreateResultDTO;
import io.github.transfusion.deployapp.dto.response.S3CredentialDTO;
import io.github.transfusion.deployapp.dto.response.StorageCredentialDTO;
import io.github.transfusion.deployapp.mappers.StorageCredentialMapper;
import io.github.transfusion.deployapp.session.SessionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class S3CreateService {

    @Autowired
    private S3TesterService s3TesterService;

    @Autowired
    private StorageCredentialRepository storageCredentialRepository;

    @Autowired
    private SessionData sessionData;

    @Autowired
    private StorageCredentialMapper storageCredentialMapper;

    public S3CreateResultDTO createS3Credential(User user, CreateS3CredentialRequest request) {
        S3Credential s3 = new S3Credential();

        s3.setUser(user);
        storageCredentialMapper.updateS3CredentialFromCreateRequest(request, s3);
            /*s3.setServer(request.getServer());
            s3.setAwsRegion(request.getAwsRegion());
            s3.setAccessKey(request.getAccessKey());
            s3.setSecretKey(request.getSecretKey());
            s3.setBucket(request.getBucket());
            s3.setName(request.getName());*/

        Instant now = Instant.now();
        s3.setCreatedOn(now);
        s3.setCheckedOn(now);

        S3TestResult s3TestResult = s3TesterService.test(s3, request.getSkipTestPublicAccess()).join();
        boolean overallSuccess = s3TestResult.getTestHeadBucketSuccess() &&
                !(!s3TestResult.getSkipTestPublicAccess() && !s3TestResult.getTestPublicAccessSuccess()) &&
                s3TestResult.getTestSignedLinkSuccess();

        if (overallSuccess) {
            UUID savedId = storageCredentialRepository.save(s3).getId();
            return new S3CreateResultDTO(overallSuccess, savedId, s3TestResult, (S3CredentialDTO) findById(savedId).get());
        } else {
            return new S3CreateResultDTO(overallSuccess, null, s3TestResult, null);
        }
    }

    public S3CreateResultDTO createS3CredentialAnonymous(User user, CreateS3CredentialRequest request) {
        S3CreateResultDTO s3CreateResultDTO = createS3Credential(user, request);
//        TODO: query by example to save on disk space...?
        if (s3CreateResultDTO.getSuccess())
            sessionData.getAnonymousCredentials().add(s3CreateResultDTO.getId());
        return s3CreateResultDTO;
    }

    private Optional<StorageCredentialDTO> findById(UUID id) {
        return storageCredentialRepository.findById(id).map(storageCredentialMapper::toDTO);
    }
}

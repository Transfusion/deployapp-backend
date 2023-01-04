package io.github.transfusion.deployapp.services;

import io.github.transfusion.deployapp.Constants;
import io.github.transfusion.deployapp.auth.CustomUserPrincipal;
import io.github.transfusion.deployapp.db.entities.FtpCredential;
import io.github.transfusion.deployapp.db.entities.S3Credential;
import io.github.transfusion.deployapp.db.entities.StorageCredential;
import io.github.transfusion.deployapp.db.entities.User;
import io.github.transfusion.deployapp.db.repositories.StorageCredentialRepository;
import io.github.transfusion.deployapp.db.repositories.UserRepository;
import io.github.transfusion.deployapp.dto.internal.FtpTestResult;
import io.github.transfusion.deployapp.dto.internal.S3TestResult;
import io.github.transfusion.deployapp.dto.request.CreateFtpCredentialRequest;
import io.github.transfusion.deployapp.dto.request.CreateS3CredentialRequest;
import io.github.transfusion.deployapp.dto.request.UpdateFtpCredentialRequest;
import io.github.transfusion.deployapp.dto.request.UpdateS3CredentialRequest;
import io.github.transfusion.deployapp.dto.response.*;
import io.github.transfusion.deployapp.exceptions.ResourceNotFoundException;
import io.github.transfusion.deployapp.mappers.StorageCredentialMapper;
import io.github.transfusion.deployapp.session.SessionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

/**
 * Looks up the storage credentials associated with a user
 */
@Service
public class StorageCredentialsService {

    public static Map<String, Class<? extends StorageCredential>> IDENTIFIER_TO_CLASS_NAME = new HashMap<>();

    static {
        IDENTIFIER_TO_CLASS_NAME.put(S3Credential.IDENTIFIER, S3Credential.class);
        IDENTIFIER_TO_CLASS_NAME.put(FtpCredential.IDENTIFIER, FtpCredential.class);
    }

    Logger logger = LoggerFactory.getLogger(StorageCredentialsService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StorageCredentialRepository storageCredentialRepository;

    @Autowired
    private StorageCredentialFetchService storageCredentialFetchService;

    public Page<StorageCredential> findPaginated(String name, List<String> identifiers, Pageable pageable) {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();

        if (authentication instanceof AnonymousAuthenticationToken) {
            return storageCredentialFetchService.findPaginatedAnonymous(name, identifiers, pageable);
        } else {
            UUID userId = ((CustomUserPrincipal) authentication.getPrincipal()).getId();
            return storageCredentialFetchService.findPaginated(userId, name, identifiers, pageable);
        }
    }

    @Autowired
    private S3CreateService s3CreateService;

    /**
     * TODO test this method, should be straightforward
     *
     * @param request
     * @return
     */
    public S3CreateResultDTO createS3Credential(CreateS3CredentialRequest request) {
//        if (StringUtils.isEmpty(request.getServer())) {
//            request.setServer("s3.amazonaws.com");
//        }
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken) {
            User user = userRepository.findById(Constants.ANONYMOUS_UID).orElseThrow(() -> new ResourceNotFoundException("User", "id", Constants.ANONYMOUS_UID));
            return s3CreateService.createS3CredentialAnonymous(user, request);
        } else {
            UUID userId = ((CustomUserPrincipal) authentication.getPrincipal()).getId();
            User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
            return s3CreateService.createS3Credential(user, request);
        }
    }

    @Autowired
    private S3TesterService s3TesterService;

    @Autowired
    private StorageCredentialMapper storageCredentialMapper;

    public S3UpdateResultDTO updateS3Credential(UUID id, UpdateS3CredentialRequest request) {
        Optional<StorageCredential> _s3 = storageCredentialRepository.findById(id);
        if (_s3.isEmpty() || !(_s3.get() instanceof S3Credential))
            throw new IllegalArgumentException(String.format("S3 credential with ID %s not found", id));

        // TODO: handle organizations!

        S3Credential s3 = (S3Credential) _s3.get();
        storageCredentialMapper.updateS3CredentialFromUpdateRequest(request, s3);

        Instant now = Instant.now();
        s3.setCreatedOn(now);
        s3.setCheckedOn(now);

        S3TestResult s3TestResult = s3TesterService.test(s3, request.getSkipTestPublicAccess()).join();
        boolean overallSuccess = s3TestResult.getTestHeadBucketSuccess() &&
                !(!s3TestResult.getSkipTestPublicAccess() && !s3TestResult.getTestPublicAccessSuccess()) &&
                s3TestResult.getTestSignedLinkSuccess();

        if (overallSuccess) {
            UUID savedId = storageCredentialRepository.save(s3).getId();
            return new S3UpdateResultDTO(overallSuccess, savedId, s3TestResult, (S3CredentialDTO) findById(savedId).get());
        } else {
            return new S3UpdateResultDTO(overallSuccess, null, s3TestResult, null);
        }
    }

    @Autowired
    private FtpCreateService ftpCreateService;

    @Autowired
    private FtpTesterService ftpTesterService;

    public FtpCreateResultDTO createFtpCredential(CreateFtpCredentialRequest request) {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken) {
            User user = userRepository.findById(Constants.ANONYMOUS_UID).orElseThrow(() -> new ResourceNotFoundException("User", "id", Constants.ANONYMOUS_UID));
            return ftpCreateService.createFtpCredentialAnonymous(user, request);
        } else {
            UUID userId = ((CustomUserPrincipal) authentication.getPrincipal()).getId();
            User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
            return ftpCreateService.createFtpCredential(user, request);
        }
    }


    public FtpUpdateResultDTO updateFtpCredential(UUID id, UpdateFtpCredentialRequest request) {
        Optional<StorageCredential> _ftp = storageCredentialRepository.findById(id);
        if (_ftp.isEmpty() || !(_ftp.get() instanceof FtpCredential))
            throw new IllegalArgumentException(String.format("S3 credential with ID %s not found", id));

        // TODO: handle organizations!

        FtpCredential ftp = (FtpCredential) _ftp.get();
//            s3.setUser(user);
        storageCredentialMapper.updateFtpCredentialFromUpdateRequest(request, ftp);

        Instant now = Instant.now();
        ftp.setCreatedOn(now);
        ftp.setCheckedOn(now);

        FtpTestResult ftpTestResult = ftpTesterService.test(ftp).join();
        boolean overallSuccess = ftpTestResult.getTestConnectionSuccess() && ftpTestResult.getTestWriteFolderSuccess()
                && ftpTestResult.getTestPublicAccessSuccess();

        if (overallSuccess) {
            UUID savedId = storageCredentialRepository.save(ftp).getId();
            return new FtpUpdateResultDTO(overallSuccess, savedId, ftpTestResult, (FtpCredentialDTO) findById(savedId).get());
        } else {
            return new FtpUpdateResultDTO(overallSuccess, null, ftpTestResult, null);
        }
    }

    @Autowired
    private SessionData sessionData;

    public void deleteStorageCredential(UUID id) {
        Optional<StorageCredential> _cred = storageCredentialRepository.findById(id);
        if (_cred.isEmpty())
            throw new IllegalArgumentException(String.format("Storage credential with ID %s not found", id));
        storageCredentialRepository.delete(_cred.get());
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken) sessionData.getAnonymousCredentials().remove(id);
    }


    public Optional<StorageCredentialDTO> findById(UUID id) {
        return storageCredentialRepository.findById(id).map(storageCredentialMapper::toDTO);
    }

    /**
     * Assigns anonymous credentials in the {@link SessionData} to the given user id
     *
     * @param userId {@link UUID} of the {@link User}
     */
    public void migrateAnonymousData(UUID userId) {
        if (sessionData.getAnonymousCredentials().isEmpty()) return;
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        storageCredentialRepository.migrateAnonymousStorageCredentials(user, sessionData.getAnonymousCredentials());
        sessionData.getAnonymousCredentials().clear();
    }
}

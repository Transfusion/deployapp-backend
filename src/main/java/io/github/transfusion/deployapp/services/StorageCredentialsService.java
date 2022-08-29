package io.github.transfusion.deployapp.services;

import io.github.transfusion.deployapp.auth.CustomUserPrincipal;
import io.github.transfusion.deployapp.auth.ResourceNotFoundException;
import io.github.transfusion.deployapp.db.entities.FtpCredential;
import io.github.transfusion.deployapp.db.entities.S3Credential;
import io.github.transfusion.deployapp.db.entities.StorageCredential;
import io.github.transfusion.deployapp.db.entities.User;
import io.github.transfusion.deployapp.db.repositories.StorageCredentialRepository;
import io.github.transfusion.deployapp.db.repositories.UserRepository;
import io.github.transfusion.deployapp.dto.internal.S3TestResult;
import io.github.transfusion.deployapp.dto.request.CreateS3CredentialRequest;
import io.github.transfusion.deployapp.dto.response.S3CreateResultDTO;
import io.github.transfusion.deployapp.dto.response.StorageCredentialDTO;
import io.github.transfusion.deployapp.mappers.StorageCredentialMapper;
import org.jetbrains.annotations.NotNull;
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

import javax.servlet.http.HttpSession;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

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

    public static String ANONYMOUS_CREDENTIALS_SESSION_ATTRIBUTE = "anonymousCredentials";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StorageCredentialRepository storageCredentialRepository;

    @Autowired
    private HttpSession session;

    public Page<StorageCredentialDTO> findPaginated(String name, List<String> identifiers, Pageable pageable) {
        // TODO: perhaps refactor this entire method using QueryDSL...
        if (identifiers == null) identifiers = new ArrayList<>();
        List<Class<? extends StorageCredential>> classes = identifiers.stream().map(id -> IDENTIFIER_TO_CLASS_NAME.get(id)).collect(Collectors.toList());

        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();

        if (authentication instanceof AnonymousAuthenticationToken) {
            Set<UUID> anonymousCredentials = getAnonymousCredentials();
            if (name == null)
                return storageCredentialRepository.findByIdIn(anonymousCredentials, classes, pageable).map(StorageCredentialMapper.instance::toDTO);
            else
                return storageCredentialRepository.findByIdInLikeName(anonymousCredentials, name, classes, pageable).map(StorageCredentialMapper.instance::toDTO);
        }

        // TODO: Add organizations

        UUID userId = ((CustomUserPrincipal) authentication.getPrincipal()).getId();
        if (name == null)
            return storageCredentialRepository.findAllByUserId(userId, classes, pageable).map(StorageCredentialMapper.instance::toDTO);
        else
            return storageCredentialRepository.findAllByUserIdLikeName(userId, name, classes, pageable).map(StorageCredentialMapper.instance::toDTO);
    }

    @Autowired
    private S3TesterService s3TesterService;

    public S3CreateResultDTO createS3Credential(CreateS3CredentialRequest request) {
//        if (StringUtils.isEmpty(request.getServer())) {
//            request.setServer("s3.amazonaws.com");
//        }

        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken) {
            // add it into the session
            Set<UUID> anonymousCredentials = getAnonymousCredentials();
            anonymousCredentials.add(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"));
            anonymousCredentials.add(UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"));

            session.setAttribute(ANONYMOUS_CREDENTIALS_SESSION_ATTRIBUTE, anonymousCredentials);
        } else {
            UUID userId = ((CustomUserPrincipal) authentication.getPrincipal()).getId();
            User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

            S3Credential s3 = new S3Credential();
            s3.setUser(user);
            s3.setServer(request.getServer());
            s3.setAwsRegion(request.getAwsRegion());
            s3.setAccessKey(request.getAccessKey());
            s3.setSecretKey(request.getSecretKey());
            s3.setBucket(request.getBucket());
            s3.setName(request.getName());

            Instant now = Instant.now();
            s3.setCreatedOn(now);
            s3.setCheckedOn(now);

            S3TestResult testResult = s3TesterService.test(s3, request.getSkipTestPublicAccess()).join();

            storageCredentialRepository.save(s3);
            return new S3CreateResultDTO(testResult);
        }

        return null;
    }

    @NotNull
    private Set<UUID> getAnonymousCredentials() {
        Set<UUID> anonymousCredentials = (Set<UUID>) session
                .getAttribute(ANONYMOUS_CREDENTIALS_SESSION_ATTRIBUTE);
        if (anonymousCredentials == null) {
            anonymousCredentials = new HashSet<>();
        }

        return anonymousCredentials;
    }


    public Optional<StorageCredentialDTO> findById(UUID id) {
        return storageCredentialRepository.findById(id).map(StorageCredentialMapper.instance::toDTO);
    }
}

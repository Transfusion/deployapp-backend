package io.github.transfusion.deployapp.services;

import io.github.transfusion.deployapp.db.entities.StorageCredential;
import io.github.transfusion.deployapp.db.repositories.StorageCredentialRepository;
import io.github.transfusion.deployapp.session.SessionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.github.transfusion.deployapp.services.StorageCredentialsService.IDENTIFIER_TO_CLASS_NAME;

@Service
public class StorageCredentialFetchService {
    @Autowired
    private StorageCredentialRepository storageCredentialRepository;

    @Autowired
    private SessionData sessionData;

    public Page<StorageCredential> findPaginatedAnonymous(String name, List<String> identifiers, Pageable pageable) {
        if (identifiers == null) identifiers = new ArrayList<>();
        List<Class<? extends StorageCredential>> classes = identifiers.stream().map(id -> IDENTIFIER_TO_CLASS_NAME.get(id)).collect(Collectors.toList());
        Set<UUID> anonymousCredentials = sessionData.getAnonymousCredentials();
        if (name == null)
            return storageCredentialRepository.findByIdIn(anonymousCredentials, classes, pageable)/*.map(StorageCredentialMapper.instance::toDTO)*/;
        else
            return storageCredentialRepository.findByIdInLikeName(anonymousCredentials, name, classes, pageable)/*.map(StorageCredentialMapper.instance::toDTO)*/;
    }

    public /*Page<StorageCredentialDTO>*/ Page<StorageCredential> findPaginated(UUID userId, String name, List<String> identifiers, Pageable pageable) {
        // TODO: perhaps refactor this entire method using QueryDSL...
        if (identifiers == null) identifiers = new ArrayList<>();
        List<Class<? extends StorageCredential>> classes = identifiers.stream().map(id -> IDENTIFIER_TO_CLASS_NAME.get(id)).collect(Collectors.toList());

        // TODO: Add organizations

        if (name == null)
            return storageCredentialRepository.findAllByUserId(userId, classes, pageable)/*.map(StorageCredentialMapper.instance::toDTO)*/;
        else
            return storageCredentialRepository.findAllByUserIdLikeName(userId, name, classes, pageable)/*.map(StorageCredentialMapper.instance::toDTO)*/;
    }
}

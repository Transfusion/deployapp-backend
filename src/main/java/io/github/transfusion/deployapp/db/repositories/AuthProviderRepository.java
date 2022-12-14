package io.github.transfusion.deployapp.db.repositories;

import io.github.transfusion.deployapp.db.entities.AuthProvider;
import io.github.transfusion.deployapp.db.entities.AuthProviderId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuthProviderRepository extends JpaRepository<AuthProvider, AuthProviderId> {
    Optional<AuthProvider> findByProviderKey(String providerKey);

    List<AuthProvider> findByUserId(UUID id);

    //    https://stackoverflow.com/questions/55579240/spring-data-find-by-property-of-a-nested-object
    long deleteByUserIdAndId_ProviderName(UUID id, String providerName);
}
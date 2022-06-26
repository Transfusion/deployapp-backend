package io.github.transfusion.deployapp.db.repositories;

import io.github.transfusion.deployapp.db.entities.AuthProvider;
import io.github.transfusion.deployapp.db.entities.AuthProviderId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthProviderRepository extends JpaRepository<AuthProvider, AuthProviderId> {
    Optional<AuthProvider> findByProviderKey(String providerKey);
}
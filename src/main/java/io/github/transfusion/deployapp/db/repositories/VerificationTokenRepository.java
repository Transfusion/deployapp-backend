package io.github.transfusion.deployapp.db.repositories;

import io.github.transfusion.deployapp.db.entities.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {
    Optional<VerificationToken> findByUserId(UUID id);
}
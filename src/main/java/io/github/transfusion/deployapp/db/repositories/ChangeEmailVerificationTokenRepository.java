package io.github.transfusion.deployapp.db.repositories;

import io.github.transfusion.deployapp.db.entities.ChangeEmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

public interface ChangeEmailVerificationTokenRepository extends JpaRepository<ChangeEmailVerificationToken, UUID> {
    Optional<ChangeEmailVerificationToken> findByUserId(UUID id);

    @Transactional
    long deleteByUserId(UUID id);
}

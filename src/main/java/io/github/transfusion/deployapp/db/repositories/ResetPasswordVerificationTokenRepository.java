package io.github.transfusion.deployapp.db.repositories;

import io.github.transfusion.deployapp.db.entities.ResetPasswordVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

public interface ResetPasswordVerificationTokenRepository extends JpaRepository<ResetPasswordVerificationToken, UUID> {
    Optional<ResetPasswordVerificationToken> findByUserId(UUID id);

    @Transactional
    long deleteByUserId(UUID id);
}

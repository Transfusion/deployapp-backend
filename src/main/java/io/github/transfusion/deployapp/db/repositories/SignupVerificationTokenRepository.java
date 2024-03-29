package io.github.transfusion.deployapp.db.repositories;

import io.github.transfusion.deployapp.db.entities.SignupVerificationToken;
//import io.github.transfusion.deployapp.db.entities.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

public interface SignupVerificationTokenRepository extends JpaRepository<SignupVerificationToken, UUID> {

    Optional<SignupVerificationToken> findByUserId(UUID id);

    @Transactional
    long deleteByUserId(UUID id);
}

package com.launchstack.auth.token;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    List<PasswordResetToken> findAllByUserId(Long userId);
}

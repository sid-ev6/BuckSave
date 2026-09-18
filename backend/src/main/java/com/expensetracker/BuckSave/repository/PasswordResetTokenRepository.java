package com.expensetracker.BuckSave.repository;

import com.expensetracker.BuckSave.entity.PasswordResetToken;
import com.expensetracker.BuckSave.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    void deleteByUser(User user);
}
package com.expensetracker.BuckSave.service;


import com.expensetracker.BuckSave.entity.PasswordResetToken;
import com.expensetracker.BuckSave.repository.PasswordResetTokenRepository;

import java.time.LocalDateTime;
import java.util.UUID;

import com.expensetracker.BuckSave.dto.AuthResponse;
import com.expensetracker.BuckSave.dto.LoginRequest;
import com.expensetracker.BuckSave.dto.UserRequest;
import com.expensetracker.BuckSave.entity.RefreshToken;
import com.expensetracker.BuckSave.entity.User;
import com.expensetracker.BuckSave.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            RefreshTokenService refreshTokenService, PasswordResetTokenRepository passwordResetTokenRepository) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }


    // ==========================================
    // REGISTER
    // ==========================================

    @Transactional
    public User register(UserRequest request) {

        if (userRepository.existsByEmail(
                request.getEmail())) {

            throw new IllegalArgumentException(
                    "Email is already registered"
            );
        }


        if (userRepository.existsByUsername(
                request.getUsername())) {

            throw new IllegalArgumentException(
                    "Username is already taken"
            );
        }


        User user = new User();

        user.setName(
                request.getName()
        );

        user.setEmail(
                request.getEmail()
        );

        user.setUsername(
                request.getUsername()
        );

        user.setPassword(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );


        User savedUser =
                userRepository.save(user);


        // Default categories are shared globally.
        // They are NOT created for each user.


        return savedUser;
    }


    // ==========================================
    // LOGIN
    // ==========================================

    public AuthResponse login(
            LoginRequest request) {

        User user =
                userRepository.findByEmail(
                                request.getLogin()
                        )
                        .orElseGet(() ->
                                userRepository
                                        .findByUsername(
                                                request.getLogin()
                                        )
                                        .orElseThrow(() ->
                                                new RuntimeException(
                                                        "Invalid username or email"
                                                ))
                        );


        try {

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getEmail(),
                            request.getPassword()
                    )
            );

        } catch (BadCredentialsException e) {

            throw new RuntimeException(
                    "Invalid username/email or password"
            );
        }


        String accessToken =
                jwtService.generateToken(
                        org.springframework.security.core.userdetails.User
                                .withUsername(
                                        user.getEmail()
                                )
                                .password(
                                        user.getPassword()
                                )
                                .roles("USER")
                                .build()
                );


        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(
                        user.getEmail()
                );


        return new AuthResponse(
                accessToken,
                refreshToken.getToken()
        );
    }


    // ==========================================
    // REFRESH ACCESS TOKEN
    // ==========================================

    public AuthResponse refreshAccessToken(
            RefreshToken refreshToken) {

        refreshTokenService.verifyExpiration(
                refreshToken
        );


        User user =
                refreshToken.getUser();


        String accessToken =
                jwtService.generateToken(
                        org.springframework.security.core.userdetails.User
                                .withUsername(
                                        user.getEmail()
                                )
                                .password(
                                        user.getPassword()
                                )
                                .roles("USER")
                                .build()
                );


        return new AuthResponse(
                accessToken,
                refreshToken.getToken()
        );
    }


@Transactional
public void forgotPassword(String email) {

    User user =
            userRepository.findByEmail(email)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "No account found with this email"
                            ));

    passwordResetTokenRepository.deleteByUser(user);

    passwordResetTokenRepository.flush();

    PasswordResetToken resetToken =
            new PasswordResetToken();

    resetToken.setToken(
            UUID.randomUUID().toString()
    );

    resetToken.setUser(user);

    resetToken.setExpiryDate(
            LocalDateTime.now().plusMinutes(15)
    );

    passwordResetTokenRepository.save(resetToken);
}

    @Transactional
    public void resetPassword(
            String token,
            String newPassword) {

        PasswordResetToken resetToken =
                passwordResetTokenRepository
                        .findByToken(token)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Invalid password reset token"
                                ));

        if (resetToken.getExpiryDate()
                .isBefore(LocalDateTime.now())) {

            passwordResetTokenRepository.delete(resetToken);

            throw new RuntimeException(
                    "Password reset token has expired"
            );
        }

        User user = resetToken.getUser();

        user.setPassword(
                passwordEncoder.encode(newPassword)
        );

        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);
    }
}
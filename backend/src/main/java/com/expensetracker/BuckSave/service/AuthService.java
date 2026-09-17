package com.expensetracker.BuckSave.service;

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
    private final CategoryService categoryService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            CategoryService categoryService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.categoryService = categoryService;
    }

    // REGISTER
    @Transactional
    public User register(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                    "Email is already registered"
            );
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException(
                    "Username is already taken"
            );
        }

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        User savedUser = userRepository.save(user);

        categoryService.createDefaultCategories(savedUser);

        return savedUser;
    }

    // LOGIN
    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getLogin())
                .orElseGet(() ->
                        userRepository.findByUsername(request.getLogin())
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

        String accessToken = jwtService.generateToken(
                org.springframework.security.core.userdetails.User
                        .withUsername(user.getEmail())
                        .password(user.getPassword())
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
    public AuthResponse refreshAccessToken(
            RefreshToken refreshToken) {

        refreshTokenService.verifyExpiration(refreshToken);

        User user = refreshToken.getUser();

        String accessToken = jwtService.generateToken(
                org.springframework.security.core.userdetails.User
                        .withUsername(user.getEmail())
                        .password(user.getPassword())
                        .roles("USER")
                        .build()
        );

        return new AuthResponse(
                accessToken,
                refreshToken.getToken()
        );
    }
}

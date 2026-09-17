package com.expensetracker.BuckSave.Controller;

import com.expensetracker.BuckSave.dto.AuthResponse;
import com.expensetracker.BuckSave.dto.LoginRequest;
import com.expensetracker.BuckSave.dto.UserRequest;
import com.expensetracker.BuckSave.dto.UserResponse;
import com.expensetracker.BuckSave.entity.RefreshToken;
import com.expensetracker.BuckSave.entity.User;
import com.expensetracker.BuckSave.service.AuthService;
import com.expensetracker.BuckSave.service.RefreshTokenService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;


    public AuthController(AuthService authService, RefreshTokenService refreshTokenService) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
    }

    // REGISTER
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody UserRequest request) {

        User user = authService.register(request);

        UserResponse response = new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail()
        );

        return ResponseEntity.ok(response);
    }

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody LoginRequest request) {

        return ResponseEntity.ok(
                authService.login(request)
        );
    }
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @RequestParam String refreshToken) {

        RefreshToken token =
                refreshTokenService.findByToken(refreshToken);

        return ResponseEntity.ok(
                authService.refreshAccessToken(token)
        );
    }
    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @RequestParam String refreshToken) {

        refreshTokenService.deleteByToken(refreshToken);

        return ResponseEntity.ok(
                "Logged out successfully"
        );
    }
}
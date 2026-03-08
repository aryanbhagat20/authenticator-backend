package com.aryanhagat.authenticator.service;

import com.aryanhagat.authenticator.dto.LoginResponse;
import com.aryanhagat.authenticator.entity.RefreshToken;
import com.aryanhagat.authenticator.exception.DuplicateEmailException;
import com.aryanhagat.authenticator.exception.InvalidCredentialsException;
import com.aryanhagat.authenticator.exception.UserNotFoundException;
import com.aryanhagat.authenticator.dto.LoginRequest;
import com.aryanhagat.authenticator.dto.SignupRequest;
import com.aryanhagat.authenticator.entity.User;
import com.aryanhagat.authenticator.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TwoFactorService twoFactorService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       TwoFactorService twoFactorService,
                       JwtService jwtService,
                       RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.twoFactorService = twoFactorService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    public void signup(SignupRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateEmailException("Email already registered");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setTwoFactorSecret(twoFactorService.generateSecret());
        user.setTwoFactorEnabled(false);

        userRepository.save(user);
    }

    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (user.isTwoFactorEnabled()) {
            // 2FA required — don't issue tokens yet
            return new LoginResponse(false, true, "OTP verification required");
        }

        // Issue both tokens
        String accessToken = jwtService.generateToken(user.getEmail());
        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(user.getEmail());

        return new LoginResponse(
                true,
                false,
                "Login successful",
                accessToken,
                refreshToken.getToken()
        );
    }

    public LoginResponse verifyLoginOtp(String email, String otp) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!user.isTwoFactorEnabled()) {
            String accessToken = jwtService.generateToken(user.getEmail());
            RefreshToken refreshToken =
                    refreshTokenService.createRefreshToken(user.getEmail());
            return new LoginResponse(
                    true, false, "Login successful",
                    accessToken, refreshToken.getToken()
            );
        }

        boolean valid = twoFactorService.verifyOtp(
                user.getTwoFactorSecret(), otp
        );

        if (!valid) {
            throw new InvalidCredentialsException("Invalid OTP");
        }

        // OTP verified — issue both tokens
        String accessToken = jwtService.generateToken(user.getEmail());
        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(user.getEmail());

        return new LoginResponse(
                true, false, "Login successful",
                accessToken, refreshToken.getToken()
        );
    }
}
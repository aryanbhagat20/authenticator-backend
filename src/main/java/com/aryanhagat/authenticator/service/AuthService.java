package com.aryanhagat.authenticator.service;
import com.aryanhagat.authenticator.dto.LoginResponse;

import com.aryanhagat.authenticator.exception.DuplicateEmailException;
import com.aryanhagat.authenticator.exception.InvalidSignupException;
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

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       TwoFactorService twoFactorService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.twoFactorService = twoFactorService;
    }

    public void signup(SignupRequest request) {

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new InvalidSignupException("Email is required");
        }

        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new InvalidSignupException("Password must be at least 6 characters");
        }

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
            return new LoginResponse(
                    false,
                    true,
                    "OTP verification required"
            );
        }

        return new LoginResponse(
                true,
                false,
                "Login successful"
        );
    }

    public LoginResponse verifyLoginOtp(String email, int otp) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!user.isTwoFactorEnabled()) {
            return new LoginResponse(true, false, "Login successful");
        }

        boolean valid = twoFactorService.verifyOtp(
                user.getTwoFactorSecret(),
                otp
        );

        if (!valid) {
            throw new InvalidCredentialsException("Invalid OTP");
        }

        return new LoginResponse(
                true,
                false,
                "Login successful"
        );
    }


}

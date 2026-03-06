package com.aryanhagat.authenticator.service;

import com.aryanhagat.authenticator.dto.LoginRequest;
import com.aryanhagat.authenticator.dto.LoginResponse;
import com.aryanhagat.authenticator.dto.SignupRequest;
import com.aryanhagat.authenticator.entity.User;
import com.aryanhagat.authenticator.exception.DuplicateEmailException;
import com.aryanhagat.authenticator.exception.InvalidCredentialsException;
import com.aryanhagat.authenticator.exception.UserNotFoundException;
import com.aryanhagat.authenticator.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

// Tells JUnit 5 to use Mockito to handle @Mock and @InjectMocks
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    // ── Create mock versions of all dependencies ──
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TwoFactorService twoFactorService;

    @Mock
    private JwtService jwtService;

    // ── Create a real AuthService with mocks injected into it ──
    @InjectMocks
    private AuthService authService;

    // ── Reusable test data ──
    private User testUser;

    // @BeforeEach runs before every single test method
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@gmail.com");
        testUser.setPassword("encodedPassword123");
        testUser.setTwoFactorEnabled(false);
        testUser.setTwoFactorSecret("TESTSECRET");
    }

    // SIGNUP TESTS
    @Test
    void signup_Success() {
        // ARRANGE — set up what the mocks return
        SignupRequest request = new SignupRequest();
        request.setEmail("newuser@gmail.com");
        request.setPassword("password123");

        // When asked if email exists → say no (empty Optional)
        when(userRepository.findByEmail("newuser@gmail.com"))
                .thenReturn(Optional.empty());

        // When asked to encode password → return a fake encoded version
        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword123");

        // When asked to generate a 2FA secret → return a fake secret
        when(twoFactorService.generateSecret())
                .thenReturn("FAKESECRET");

        // When save is called → return whatever user object is passed in
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // ACT — call the method we're testing
        authService.signup(request);

        // ASSERT — verify save was called exactly once
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void signup_ThrowsDuplicateEmailException_WhenEmailAlreadyExists() {
        // ARRANGE
        SignupRequest request = new SignupRequest();
        request.setEmail("test@gmail.com");
        request.setPassword("password123");

        // Email already exists in the database
        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(testUser));

        // ACT + ASSERT — assertThatThrownBy checks that the exception IS thrown
        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessage("Email already registered");

        // Verify save was NEVER called — we should not save a duplicate user
        verify(userRepository, never()).save(any(User.class));
    }


    // LOGIN TESTS
    @Test
    void login_Success_WithoutTwoFactor() {
        // ARRANGE
        LoginRequest request = new LoginRequest();
        request.setEmail("test@gmail.com");
        request.setPassword("password123");

        // User exists
        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(testUser));

        // Password matches
        when(passwordEncoder.matches("password123", "encodedPassword123"))
                .thenReturn(true);

        // JWT generated
        when(jwtService.generateToken("test@gmail.com"))
                .thenReturn("fake.jwt.token");

        // ACT
        LoginResponse response = authService.login(request);

        // ASSERT
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.isOtpRequired()).isFalse();
        assertThat(response.getToken()).isEqualTo("fake.jwt.token");
        assertThat(response.getMessage()).isEqualTo("Login successful");
    }

    @Test
    void login_RequiresOtp_WhenTwoFactorEnabled() {
        // ARRANGE
        testUser.setTwoFactorEnabled(true); // 2FA is ON for this user

        LoginRequest request = new LoginRequest();
        request.setEmail("test@gmail.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(testUser));

        when(passwordEncoder.matches("password123", "encodedPassword123"))
                .thenReturn(true);

        // ACT
        LoginResponse response = authService.login(request);

        // ASSERT — should ask for OTP, not return a token
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.isOtpRequired()).isTrue();
        assertThat(response.getToken()).isNull();

        // JWT should NEVER be generated at this point
        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    void login_ThrowsUserNotFoundException_WhenEmailNotFound() {
        // ARRANGE
        LoginRequest request = new LoginRequest();
        request.setEmail("nobody@gmail.com");
        request.setPassword("password123");

        // No user found
        when(userRepository.findByEmail("nobody@gmail.com"))
                .thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void login_ThrowsInvalidCredentialsException_WhenPasswordWrong() {
        // ARRANGE
        LoginRequest request = new LoginRequest();
        request.setEmail("test@gmail.com");
        request.setPassword("wrongpassword");

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(testUser));

        // Password does NOT match
        when(passwordEncoder.matches("wrongpassword", "encodedPassword123"))
                .thenReturn(false);

        // ACT + ASSERT
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");
    }


    // VERIFY LOGIN OTP TESTS
    @Test
    void verifyLoginOtp_Success() {
        // ARRANGE
        testUser.setTwoFactorEnabled(true);

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(testUser));

        // OTP is valid
        when(twoFactorService.verifyOtp("TESTSECRET", "123456"))
                .thenReturn(true);

        when(jwtService.generateToken("test@gmail.com"))
                .thenReturn("fake.jwt.token");

        // ACT
        LoginResponse response = authService.verifyLoginOtp("test@gmail.com", "123456");

        // ASSERT
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getToken()).isEqualTo("fake.jwt.token");
    }

    @Test
    void verifyLoginOtp_ThrowsInvalidCredentialsException_WhenOtpWrong() {
        // ARRANGE
        testUser.setTwoFactorEnabled(true);

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(testUser));

        // OTP is INVALID
        when(twoFactorService.verifyOtp("TESTSECRET", "000000"))
                .thenReturn(false);

        // ACT + ASSERT
        assertThatThrownBy(() -> authService.verifyLoginOtp("test@gmail.com", "000000"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid OTP");
    }
}
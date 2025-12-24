package com.example.usersmanage.services;

import com.example.usersmanage.DOA.RegisterRequest;
import com.example.usersmanage.DOA.UserInfo;
import com.example.usersmanage.config.JwtUtil;
import com.example.usersmanage.config.TokenBlacklistService;
import com.example.usersmanage.entities.Users;
import com.example.usersmanage.repositories.UsersSqlRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("User Service Tests")
class UserServiceTest {

    @Mock
    private UsersSqlRepo repo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private SmtpService smtp;

    @InjectMocks
    private UserService userService;

    private Users user;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        user = new Users();
        user.setFirstname("John");
        user.setLastname("Doe");
        user.setEmail("john.doe@example.com");
        user.setPassword("encodedPassword");

        registerRequest = new RegisterRequest();
        registerRequest.setFirstname("John");
        registerRequest.setLastname("Doe");
        registerRequest.setEmail("john.doe@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setPhone("123456789");
    }

    @Test
    @DisplayName("Register - Success")
    void testRegister_Success() {
        when(repo.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        boolean result = userService.register(registerRequest);

        assertTrue(result);
        verify(repo, times(1)).save(any(Users.class));
    }

    @Test
    @DisplayName("Register - Email already exists")
    void testRegister_EmailExists() {
        when(repo.existsByEmail(anyString())).thenReturn(true);

        boolean result = userService.register(registerRequest);

        assertFalse(result);
        verify(repo, never()).save(any(Users.class));
    }

    @Test
    @DisplayName("Login - Success")
    void testLogin_Success() {
        when(repo.findFirstByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(anyString())).thenReturn("mockToken");

        UserInfo result = userService.login("john.doe@example.com", "password123");

        assertNotNull(result);
        assertEquals("mockToken", result.getToken());
        assertEquals("John", result.getUser().getFirstname());
    }

    @Test
    @DisplayName("Login - Wrong Password")
    void testLogin_WrongPassword() {
        when(repo.findFirstByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        UserInfo result = userService.login("john.doe@example.com", "wrongPassword");

        assertNull(result);
    }

    @Test
    @DisplayName("Log Out - Success")
    void testLogOut() {
        userService.logOut("mockToken");
        verify(tokenBlacklistService, times(1)).blacklist("mockToken");
    }

    @Test
    @DisplayName("Verification Code - Success")
    void testVerificationCode_Success() {
        when(repo.findFirstByEmail(anyString())).thenReturn(Optional.of(user));

        String code = userService.VerificationCode("john.doe@example.com");

        assertNotNull(code);
        assertEquals(6, code.length());
        verify(smtp, times(1)).SendEmail(eq("john.doe@example.com"), anyString(), anyString());
    }

    @Test
    @DisplayName("Verify Code - Success")
    void testVerifyCode_Success() {
        when(repo.findFirstByEmail(anyString())).thenReturn(Optional.of(user));

        // Need to generate code first to store in resetTokenStorage
        String code = userService.VerificationCode("john.doe@example.com");

        boolean result = userService.verifyCode("john.doe@example.com", code);

        assertTrue(result);
    }

    @Test
    @DisplayName("Reset Password - Success")
    void testResetPassword_Success() {
        when(repo.findFirstByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");

        String code = userService.VerificationCode("john.doe@example.com");

        boolean result = userService.resetPassword(code, "newPassword123");

        assertTrue(result);
        verify(repo, times(1)).save(user);
        assertEquals("newEncodedPassword", user.getPassword());
    }
}

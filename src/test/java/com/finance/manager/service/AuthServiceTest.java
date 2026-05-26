package com.finance.manager.service;

import com.finance.manager.dto.request.LoginRequest;
import com.finance.manager.dto.request.RegisterRequest;
import com.finance.manager.dto.response.MessageResponse;
import com.finance.manager.entity.User;
import com.finance.manager.exception.DuplicateResourceException;
import com.finance.manager.exception.UnauthorizedException;
import com.finance.manager.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

// tests for AuthService
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // setting up test data
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("john@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setFullName("John Doe");
        testUser.setPhoneNumber("1234567890");

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("john@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFullName("John Doe");
        registerRequest.setPhoneNumber("1234567890");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("john@example.com");
        loginRequest.setPassword("password123");
    }

    @Test
    void testRegisterSuccess() {
        // register a new user and make sure it works
        when(userRepository.existsByUsername("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        MessageResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertTrue(response.getMessage().toLowerCase().contains("success")
                || response.getMessage().toLowerCase().contains("registered"));
        assertEquals(1L, response.getUserId());
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void testRegisterDuplicateUsername() {
        // trying to register with existing username should throw exception
        when(userRepository.existsByUsername("john@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> {
            authService.register(registerRequest);
        });

        // make sure we never tried to save
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLoginSuccess() {
        // login with correct credentials
        when(userRepository.findByUsername("john@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(request.getSession(true)).thenReturn(session);

        MessageResponse response = authService.login(loginRequest, request);

        assertNotNull(response);
        // should set userId in session
        verify(session).setAttribute(eq("userId"), eq(1L));
    }

    @Test
    void testLoginInvalidUsername() {
        // user doesnt exist
        when(userRepository.findByUsername("john@example.com")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> {
            authService.login(loginRequest, request);
        });
    }

    @Test
    void testLoginWrongPassword() {
        // user exists but password is wrong
        when(userRepository.findByUsername("john@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> {
            authService.login(loginRequest, request);
        });
    }

    @Test
    void testGetCurrentUserSuccess() {
        // session has userId, user exists in db
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        User currentUser = authService.getCurrentUser(request);

        assertNotNull(currentUser);
        assertEquals("john@example.com", currentUser.getUsername());
        assertEquals("John Doe", currentUser.getFullName());
    }

    @Test
    void testGetCurrentUserNoSession() {
        // no session means user is not logged in
        when(request.getSession(false)).thenReturn(null);

        assertThrows(UnauthorizedException.class, () -> {
            authService.getCurrentUser(request);
        });
    }
}

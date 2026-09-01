package com.vericart.service;

import com.vericart.common.Result;
import com.vericart.dto.LoginRequest;
import com.vericart.dto.LoginResponse;
import com.vericart.dto.RegisterRequest;
import com.vericart.entity.User;
import com.vericart.exception.BusinessException;
import com.vericart.mapper.UserMapper;
import com.vericart.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private AuditLogService auditLogService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User mockUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@vericart.ai");
        registerRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@vericart.ai");
        loginRequest.setPassword("password123");

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        mockUser.setEmail("test@vericart.ai");
        mockUser.setPassword("$2a$encodedpasswordhash");
        mockUser.setRole("CUSTOMER");
        mockUser.setStatus(1);
    }

    @Test
    @DisplayName("Should register a new user successfully")
    void shouldRegisterNewUserSuccessfully() {
        when(userMapper.findByEmail(registerRequest.getEmail())).thenReturn(null);
        when(userMapper.findByUsername(registerRequest.getUsername())).thenReturn(null);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("$2a$hashed");
        when(jwtTokenProvider.generateToken(any(), any(), any())).thenReturn("mock.jwt.token");

        // Simulate insert assigning an ID
        doAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return 1;
        }).when(userMapper).insert(any(User.class));

        Result<LoginResponse> result = authService.register(registerRequest);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals("mock.jwt.token", result.getData().getToken());
        assertEquals("testuser", result.getData().getUsername());

        verify(userMapper).insert(any(User.class));
    }

    @Test
    @DisplayName("Should throw when registering with existing email")
    void shouldRejectDuplicateEmail() {
        when(userMapper.findByEmail(registerRequest.getEmail())).thenReturn(mockUser);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.register(registerRequest));
        assertEquals(400, ex.getCode());
        assertTrue(ex.getMessage().contains("Email already registered"));
    }

    @Test
    @DisplayName("Should throw when registering with existing username")
    void shouldRejectDuplicateUsername() {
        when(userMapper.findByEmail(registerRequest.getEmail())).thenReturn(null);
        when(userMapper.findByUsername(registerRequest.getUsername())).thenReturn(mockUser);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.register(registerRequest));
        assertEquals(400, ex.getCode());
        assertTrue(ex.getMessage().contains("Username already taken"));
    }

    @Test
    @DisplayName("Should login successfully with correct credentials")
    void shouldLoginSuccessfully() {
        when(userMapper.findByEmail(loginRequest.getEmail())).thenReturn(mockUser);
        when(passwordEncoder.matches(loginRequest.getPassword(), mockUser.getPassword())).thenReturn(true);
        when(jwtTokenProvider.generateToken(1L, "test@vericart.ai", "CUSTOMER")).thenReturn("jwt.token.here");

        Result<LoginResponse> result = authService.login(loginRequest);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals("jwt.token.here", result.getData().getToken());
    }

    @Test
    @DisplayName("Should throw when email not found during login")
    void shouldRejectUnknownEmail() {
        when(userMapper.findByEmail(loginRequest.getEmail())).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.login(loginRequest));
        assertEquals(401, ex.getCode());
    }

    @Test
    @DisplayName("Should throw when password is incorrect")
    void shouldRejectWrongPassword() {
        when(userMapper.findByEmail(loginRequest.getEmail())).thenReturn(mockUser);
        when(passwordEncoder.matches(loginRequest.getPassword(), mockUser.getPassword())).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.login(loginRequest));
        assertEquals(401, ex.getCode());
    }

    @Test
    @DisplayName("Should throw when account is disabled (status=0)")
    void shouldRejectDisabledAccount() {
        mockUser.setStatus(0);
        when(userMapper.findByEmail(loginRequest.getEmail())).thenReturn(mockUser);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.login(loginRequest));
        assertEquals(403, ex.getCode());
        assertTrue(ex.getMessage().contains("disabled"));
    }

    @Test
    @DisplayName("Register should assign CUSTOMER role by default")
    void shouldAssignCustomerRole() {
        when(userMapper.findByEmail(registerRequest.getEmail())).thenReturn(null);
        when(userMapper.findByUsername(registerRequest.getUsername())).thenReturn(null);
        when(passwordEncoder.encode(any())).thenReturn("hash");
        when(jwtTokenProvider.generateToken(any(), any(), any())).thenReturn("token");

        doAnswer(inv -> {
            User u = inv.getArgument(0);
            assertEquals("CUSTOMER", u.getRole());
            assertEquals(1, u.getStatus());
            u.setId(5L);
            return 1;
        }).when(userMapper).insert(any(User.class));

        Result<LoginResponse> result = authService.register(registerRequest);
        assertEquals(200, result.getCode());
    }
}

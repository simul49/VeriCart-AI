package com.vericart.service;

import com.vericart.common.Result;
import com.vericart.dto.LoginRequest;
import com.vericart.dto.LoginResponse;
import com.vericart.dto.RegisterRequest;
import com.vericart.entity.User;
import com.vericart.exception.BusinessException;
import com.vericart.mapper.UserMapper;
import com.vericart.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public Result<LoginResponse> register(RegisterRequest request) {
        if (userMapper.findByEmail(request.getEmail()) != null) {
            throw new BusinessException(400, "Email already registered");
        }
        if (userMapper.findByUsername(request.getUsername()) != null) {
            throw new BusinessException(400, "Username already taken");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("CUSTOMER");
        user.setStatus(1);

        userMapper.insert(user);

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole());
        LoginResponse resp = new LoginResponse(token, user.getId(), user.getUsername(), user.getEmail(), user.getRole());
        return Result.success("Registration successful", resp);
    }

    public Result<LoginResponse> login(LoginRequest request) {
        User user = userMapper.findByEmail(request.getEmail());
        if (user == null) {
            throw new BusinessException(401, "Invalid email or password");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(403, "Account is disabled");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "Invalid email or password");
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole());
        LoginResponse resp = new LoginResponse(token, user.getId(), user.getUsername(), user.getEmail(), user.getRole());
        return Result.success(resp);
    }
}

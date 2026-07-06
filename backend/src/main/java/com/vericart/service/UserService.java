package com.vericart.service;

import com.vericart.entity.User;
import com.vericart.exception.BusinessException;
import com.vericart.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public User getById(Long id) {
        User user = userMapper.findById(id);
        if (user == null) throw new BusinessException(404, "User not found");
        user.setPassword(null);
        return user;
    }

    public User updateProfile(Long id, String phone, String avatar) {
        User user = userMapper.findById(id);
        if (user == null) throw new BusinessException(404, "User not found");
        user.setPhone(phone);
        user.setAvatar(avatar);
        userMapper.updateProfile(user);
        user.setPassword(null);
        return user;
    }

    public void changePassword(Long id, String oldPassword, String newPassword) {
        User user = userMapper.findById(id);
        if (user == null) throw new BusinessException(404, "User not found");
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(400, "Old password is incorrect");
        }
        userMapper.updatePassword(id, passwordEncoder.encode(newPassword));
    }
}

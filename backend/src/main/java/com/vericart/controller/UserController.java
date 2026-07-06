package com.vericart.controller;

import com.vericart.common.Result;
import com.vericart.entity.User;
import com.vericart.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public Result<User> profile(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(userService.getById(userId));
    }

    @PutMapping("/profile")
    public Result<User> updateProfile(Authentication auth,
                                       @RequestBody Map<String, String> body) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(userService.updateProfile(userId,
                body.get("phone"), body.get("avatar")));
    }

    @PutMapping("/password")
    public Result<Void> changePassword(Authentication auth,
                                        @RequestBody Map<String, String> body) {
        Long userId = (Long) auth.getPrincipal();
        userService.changePassword(userId,
                body.get("oldPassword"), body.get("newPassword"));
        return Result.success("Password changed", null);
    }
}

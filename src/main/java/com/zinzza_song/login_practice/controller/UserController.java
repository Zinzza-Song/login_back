package com.zinzza_song.login_practice.controller;

import com.zinzza_song.login_practice.dto.UserRequestDTO;
import com.zinzza_song.login_practice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth") // http://localhost:8080/api/auth/**
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/signup")
    public String signup(@RequestBody UserRequestDTO userRequestDTO) {
        userService.signUp(userRequestDTO);

        return "signup-success";
    }
}

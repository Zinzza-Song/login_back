package com.zinzza_song.login_practice.controller;

import com.zinzza_song.login_practice.dto.LoginRequestDTO;
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

    /**
     * 회원가입을 위한 Controller
     *
     * @param dto 회원 가입을 위한 유저 DTO
     * @return 메소드 정상 수행 성공 시 signup-success 메시지 출력
     */
    @PostMapping("/signup")
    public String signup(@RequestBody UserRequestDTO dto) {
        userService.signUp(dto);

        return "signup-success";
    }

    /**
     * 로그인을 위한 Controller
     *
     * @param dto 로그인을 위한 로그인 DTO
     * @return 로그인에 성공하면 인증 토큰을 반환
     */
    @PostMapping("/login")
    public String login(@RequestBody LoginRequestDTO dto) {
        return userService.login(dto);
    }
}

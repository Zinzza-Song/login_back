package com.zinzza_song.login_practice.controller;

import com.zinzza_song.login_practice.dto.LoginRequestDTO;
import com.zinzza_song.login_practice.dto.LoginResponseDTO;
import com.zinzza_song.login_practice.dto.UserRequestDTO;
import com.zinzza_song.login_practice.entity.User;
import com.zinzza_song.login_practice.repository.UserRepository;
import com.zinzza_song.login_practice.service.UserService;
import com.zinzza_song.login_practice.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth") // http://localhost:8080/api/auth/**
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    /**
     * 회원 가입
     *
     * @param dto 회원 가입을 위한 user의 정보를 지닌 회원 가입 요청 DTO 객체
     * @return 회원 가입 수행 성공 시 회원 가입 성공 메시지 출력
     */
    @PostMapping("/signup")
    public String signup(@RequestBody UserRequestDTO dto) {
        userService.signUp(dto);

        return "signup-success";
    }

    /**
     * 로그인
     *
     * @param dto 로그인 시도를 한 user의 정보를 지닌 로그인 요청 DTO 객체
     * @return 로그인 성공시 user의 Access 토큰과 Refresh 토큰을 지닌 로그인 응답 DTO 객체 반환
     */
    @PostMapping("/login")
    public LoginResponseDTO login(@RequestBody LoginRequestDTO dto) {
        return userService.login(dto);
    }

    /**
     * 토큰 재발급
     * @param refreshToken 기간이 만료된 Refresh 토큰
     * @return 재발급 성공시 새로운 Access 토큰과 Refresh 토큰을 지닌 로그인 응답 DTO 객체를 반환
     */
    @PostMapping("/refresh")
    public LoginResponseDTO refresh(@RequestHeader("Authorization") String refreshToken) {
        String token = refreshToken.replace("Bearer ", "");

        if(!jwtTokenProvider.validateToken(token))
            throw new RuntimeException("Refresh 토큰이 유효하지 않습니다.");

        String username = jwtTokenProvider.getUsernameFromToken(token);
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자가 존재하지 않습니다."));

        if(!token.equals(user.getRefreshToken()))
            throw new RuntimeException("서버에 저장된 Refresh 토큰과 일치하지 않습니다.");

        String newAccessToken = jwtTokenProvider.generateAccessToken(username);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(username);
        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);

        return ResponseEntity.ok(new LoginResponseDTO(newAccessToken, newRefreshToken)).getBody();
    }

    /**
     * 로그아웃
     * 
     * @param accessToken 로그아웃을 시도한 user의 Access 토큰
     * @return 로그아웃 수행 성공 시 로그아웃 메시지 출력
     */
    @PostMapping("/logout")
    public String logout(@RequestHeader("Authorization") String accessToken) {
        String token = accessToken.replace("Bearer ", "");
        String username =  jwtTokenProvider.getUsernameFromToken(token);
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자가 존재하지 않습니다."));

        user.setRefreshToken(null);
        userRepository.save(user);

        return "logout-success";
    }
}

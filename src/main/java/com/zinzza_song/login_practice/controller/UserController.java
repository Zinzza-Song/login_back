package com.zinzza_song.login_practice.controller;

import com.zinzza_song.login_practice.dto.LoginRequestDTO;
import com.zinzza_song.login_practice.dto.LoginResponseDTO;
import com.zinzza_song.login_practice.dto.UserRequestDTO;
import com.zinzza_song.login_practice.entity.User;
import com.zinzza_song.login_practice.repository.UserRepository;
import com.zinzza_song.login_practice.service.UserService;
import com.zinzza_song.login_practice.util.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
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
     * @param res HTTP 응답 객체
     * @return 로그인 성공시 user의 Access 토큰과 Refresh 토큰이 담긴 응답 객체를 반환
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO dto,
                                                  HttpServletResponse res) {
        LoginResponseDTO tokens = userService.login(dto);

        Cookie refreshCookie = new Cookie("refreshToken", tokens.getRefreshToken());
        refreshCookie.setPath("/");
        refreshCookie.setHttpOnly(true);
        refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7일
        res.addCookie(refreshCookie);

        return ResponseEntity.ok(new LoginResponseDTO(tokens.getAccessToken(), null));
    }

    /**
     * 토큰 재발급
     *
     * @param req 토큰 재발급 요청 객체
     * @return 재발급 성공시 새로운 Access 토큰이 담긴 응답 객체를 반환
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest req) {
        String refreshToken = null;
        if (req.getCookies() != null) {
            for (Cookie cookie : req.getCookies()) {
                if (cookie.getName().equals("refreshToken")) refreshToken = cookie.getValue();
            }
        }

        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 refresh 토큰입니다.");
        }

        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자가 존재하지 않습니다."));

        if (!refreshToken.equals(user.getRefreshToken())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("서버 저장 refresh 토큰과 불일치");
        }

        // Access & Refresh Token 새로 발급
        String newAccessToken = jwtTokenProvider.generateAccessToken(username, user.getRole());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(username);

        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);

        // HttpOnly 쿠키로 Refresh Token 전달
        ResponseCookie cookie = ResponseCookie.from("refreshToken", newRefreshToken)
                .httpOnly(true)
                .secure(false)   // 개발환경이라면 false, 배포 시 true
                .sameSite("Lax") // 중요!
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new LoginResponseDTO(newAccessToken, null));
    }

    /**
     * 로그아웃
     *
     * @param accessToken 로그아웃을 시도한 user의 Access 토큰
     * @param res 응답처리를 위한 응답 객체
     * @return 로그아웃 수행 성공 시 로그아웃 메시지가 담긴 응답 객체를 반환
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String accessToken,
                                         HttpServletResponse res) {
        String token = accessToken.replace("Bearer ", "");
        String username = jwtTokenProvider.getUsernameFromToken(token);
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자가 존재하지 않습니다."));

        user.setRefreshToken(null);
        userRepository.save(user);

        Cookie refreshCookie = new Cookie("refreshToken", null);
        refreshCookie.setPath("/");
        refreshCookie.setHttpOnly(true);
        refreshCookie.setMaxAge(0);
        res.addCookie(refreshCookie);

        return ResponseEntity.ok("logout-success");
    }
}

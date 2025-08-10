package com.zinzza_song.login_practice.service;

import com.zinzza_song.login_practice.dto.LoginRequestDTO;
import com.zinzza_song.login_practice.dto.LoginResponseDTO;
import com.zinzza_song.login_practice.dto.UserRequestDTO;
import com.zinzza_song.login_practice.entity.User;
import com.zinzza_song.login_practice.repository.UserRepository;
import com.zinzza_song.login_practice.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 회원 가입
     *
     * @param dto 회원 가입을 위한 user의 정보를 지닌 회원 가입 요청 DTO 객체
     */
    public void signUp(UserRequestDTO dto) {
        if (userRepository.findByUsername(dto.getUsername()).isPresent())
            throw new RuntimeException("이미 존재하는 사용자 입니다.");

        User user = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role("ROLE_USER")
                .build();

        userRepository.save(user);
    }

    /**
     * 로그인
     *
     * @param dto 로그인 시도를 한 user의 정보를 지닌 로그인 요청 DTO 객체
     * @return 로그인 성공시 user의 Access 토큰과 Refresh 토큰을 지닌 로그인 응답 DTO 객체 반환
     */
    public LoginResponseDTO login(LoginRequestDTO dto) {
        User user = userRepository
                .findByUsername(dto.getUsername())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if(!passwordEncoder.matches(dto.getPassword(), user.getPassword()))
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");

        String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());

        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return new LoginResponseDTO(accessToken, refreshToken);
    }
}

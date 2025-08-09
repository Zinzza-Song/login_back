package com.zinzza_song.login_practice.service;

import com.zinzza_song.login_practice.dto.LoginRequestDTO;
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
     * 회원 가입을 위한 Service
     *
     * @param dto user 테이블에 저장할 데이터를 지닌 DTO
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
     * 로그인을 위한 Service
     *
     * @param dto 로그인을 시도하기 위하여 작성한 데이터를 지닌 DTO
     * @return 로그인에 성공하면 인증 토큰을 반환
     */
    public String login(LoginRequestDTO dto) {
        User user = userRepository
                .findByUsername(dto.getUsername())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if(!passwordEncoder.matches(dto.getPassword(), user.getPassword()))
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");

        return jwtTokenProvider.generateToken(user.getUsername());
    }
}

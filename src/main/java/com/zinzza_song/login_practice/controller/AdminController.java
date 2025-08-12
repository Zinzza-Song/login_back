package com.zinzza_song.login_practice.controller;

import com.zinzza_song.login_practice.entity.User;
import com.zinzza_song.login_practice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final UserRepository userRepository;

    /**
     * 관리자 전용 대시보드
     *
     * @return 접속 성공 메시지 출력
     */
    @GetMapping("/dashboard")
    public String adminOnly() {
        return "관리자 전용 대시보드입니다.";
    }

    /**
     * 가입된 user 리스트
     *
     * @return 현재 DB에 저장된 모든 user들의 데이터들을 List 형식으로 봔환
     */
    @GetMapping("/users")
    public List<User> getUsers() {
        return userRepository.findAll();
    }
}

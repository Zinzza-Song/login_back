package com.zinzza_song.login_practice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/private")
public class TestController {
    /**
     * 인증된 모든 user가 들어오는 페이지
     *
     * @return 접속 성공 메시지 출력
     */
    @GetMapping("/hello")
    public String privateHello(){
        return "인증된 사용자";
    }
}

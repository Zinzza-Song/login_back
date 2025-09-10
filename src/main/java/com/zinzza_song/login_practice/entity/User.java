package com.zinzza_song.login_practice.entity;

import jakarta.persistence.*;
import lombok.*;

// User 테이블을 위한 Entity
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username; // id

    private String password; // 비밀번호

    @Column(nullable = false)
    private String role; // 권한

    @Column(length = 500)
    private String refreshToken;

    public User(String username, String password, String roleUser) {
        this.username = username;
        this.role = roleUser;

    }
}

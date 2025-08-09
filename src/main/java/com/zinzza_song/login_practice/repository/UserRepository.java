package com.zinzza_song.login_practice.repository;

import com.zinzza_song.login_practice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// User 테이블을 사용하기 위한 Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}

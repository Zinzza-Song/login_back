package com.zinzza_song.login_practice.util;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {
    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256); // 토큰 키값을 랜덤하게 설정

    /**
     * Access 토큰 생성
     *
     * @param username 로그인 및 토큰을 재발급 받을 user의 ID
     * @return Access 토큰 생성
     */
    public String generateAccessToken(String username) {
        long expiration_30m = 1000L * 60 * 60;

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration_30m))
                .signWith(key)
                .compact();
    }

    /**
     * Refresh 토큰 생성
     *
     * @param username 로그인 및 Refresh 토큰을 재발급 받을 user의 ID
     * @return Refresh 토큰 생성
     */
    public String generateRefreshToken(String username) {
        long expiration_7d = 1000L * 60 * 60 * 24 * 7;

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration_7d))
                .signWith(key)
                .compact();
    }

    /**
     * 토큰 복호화
     *
     * @param token 인증 요청할 암호화 된  토큰
     * @return 복호화 된 토큰
     */
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
    }

    /**
     * 토큰의 유효성 검사
     *
     * @param token 유효성 검사를 할 토큰
     * @return 유효성 검사에 통과 되면 true 값을 반환 실패 하면 false 값을 반환
     */
    public boolean validateToken(String token) {
        try{
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e){
            return false;
        }
    }
}

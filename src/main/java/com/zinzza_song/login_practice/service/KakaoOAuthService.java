package com.zinzza_song.login_practice.service;

import com.zinzza_song.login_practice.dto.LoginResponseDTO;
import com.zinzza_song.login_practice.entity.User;
import com.zinzza_song.login_practice.repository.UserRepository;
import com.zinzza_song.login_practice.util.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class KakaoOAuthService {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String clientUri;

    public LoginResponseDTO kakaoLogin(String code, HttpServletResponse res) {
        String tokenUrl = "https://kauth.kakao.com/oauth/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("redirect_uri", clientUri);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> tokenReq = new HttpEntity<>(params, headers);
        ResponseEntity<Map> tokenRes = restTemplate.postForEntity(tokenUrl, tokenReq, Map.class);

        String kakaoAccessToken = (String) Objects.requireNonNull(tokenRes.getBody()).get("access_token");

        HttpHeaders userInfoHeaders = new HttpHeaders();
        userInfoHeaders.setBearerAuth(kakaoAccessToken);
        HttpEntity<?> userInfoReq = new HttpEntity<>(userInfoHeaders);

        ResponseEntity<Map> userInfoRes = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET,
                userInfoReq,
                Map.class
        );

        Map<String, Object> kakaoAccount = (Map<String, Object>) userInfoRes.getBody().get("kakao_account");

        String email = (String) kakaoAccount.get("email");
        String username = "kakao_" + userInfoRes.getBody().get("id");

        System.out.println("username: " + username);
        System.out.println("mail: " + email);

        User user = userRepository.findByUsername(username)
                .orElseGet(() -> {
                   User newUser = new User(username, null, "ROLE_USER");

                   return userRepository.save(newUser);
                });

        String accessToken = jwtTokenProvider.generateAccessToken(username, user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(username);

        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60);
        refreshCookie.setHttpOnly(true);
        res.addCookie(refreshCookie);

        return new LoginResponseDTO(accessToken, null);
    }
}

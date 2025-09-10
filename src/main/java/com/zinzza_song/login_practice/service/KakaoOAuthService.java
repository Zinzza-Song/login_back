package com.zinzza_song.login_practice.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zinzza_song.login_practice.dto.LoginResponseDTO;
import com.zinzza_song.login_practice.entity.User;
import com.zinzza_song.login_practice.repository.UserRepository;
import com.zinzza_song.login_practice.util.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KakaoOAuthService {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String clientUri;

    /**
     * 카카오 소셜 로그인
     *
     * @param code 카카오에서 발급 받은 일회용 인증 코드
     * @param res 클라이언트에 보낼 응답 객체
     * @return 로그인 응답 DTO(Access Token만 전달)
     */
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

        ResponseEntity<Map<String, Object>> tokenRes = restTemplate.exchange(
                tokenUrl,
                HttpMethod.POST,
                tokenReq,
                new ParameterizedTypeReference<>(){}
        );

        Map<String, Object> tokenBody = tokenRes.getBody();
        if (tokenBody == null || !tokenBody.containsKey("access_token")) {
            throw new RuntimeException("Kakao access token 요청 실패");
        }
        String kakaoAccessToken = (String) tokenBody.get("access_token");

        HttpHeaders userInfoHeaders = new HttpHeaders();
        userInfoHeaders.setBearerAuth(kakaoAccessToken);
        HttpEntity<?> userInfoReq = new HttpEntity<>(userInfoHeaders);

        ResponseEntity<Map<String, Object>> userInfoRes = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET,
                userInfoReq,
                new ParameterizedTypeReference<>() {}
        );

        Map<String, Object> userInfoBody = userInfoRes.getBody();
        Map<String, Object> kakaoAccount = Collections.emptyMap();
        if (userInfoBody != null && userInfoBody.get("kakao_account") != null) {
            kakaoAccount = objectMapper.convertValue(
                    userInfoBody.get("kakao_account"),
                    new TypeReference<>() {}
            );
        }

        String email = (String) kakaoAccount.get("email");
        String username = "kakao_" + userInfoBody.get("id");

        System.out.println("username: " + username);
        System.out.println("mail: " + email);

        User user = userRepository.findByUsername(username)
                .orElseGet(() -> {
                    User newUser = new User(username, "ROLE_USER");
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

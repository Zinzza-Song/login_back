package com.zinzza_song.login_practice.security;

import com.zinzza_song.login_practice.entity.User;
import com.zinzza_song.login_practice.repository.UserRepository;
import com.zinzza_song.login_practice.util.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter { // JWT 토큰 인증 필터
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest req,
                                    @NonNull HttpServletResponse res,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        String token = resolveToken(req);
        if(token != null && jwtTokenProvider.validateToken(token)) {
            String username = jwtTokenProvider.getUsernameFromToken(token);
            User user = userRepository.findByUsername(username).orElse(null);

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if(user != null) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails,
                                null,
                                userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        chain.doFilter(req, res);
    }

    /**
     * 인증 요청한 user의 Access 토큰을 복호화 하는 함수
     *
     * @param req 인증 요철을 위하여 서버로 보낸 HTTP 요청 객체
     * @return HTTP 요청 객체의 Header에서 Access 토큰을 추출 성공시 추출된 토큰을 반환 실패시 null값을 반환
     */
    private String resolveToken(HttpServletRequest req) {
        String bearer = req.getHeader("Authorization");
        if(bearer != null && bearer.startsWith("Bearer ")) return bearer.substring(7);

        return null;
    }
}

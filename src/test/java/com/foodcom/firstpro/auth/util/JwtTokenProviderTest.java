package com.foodcom.firstpro.auth.util;

import com.foodcom.firstpro.auth.dto.TokenInfo;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.security.Key;
import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private String secretKey = "c2lsdmVybmluZS10ZWNoLXNwcmluZy1ib290LWp3dC10dXRvcmlhbC1zZWNyZXQtc2lsdmVybmluZS10ZWNoLXNwcmluZy1ib290LWp3dC10dXRvcmlhbC1zZWNyZXQK";

    private long accessExpiration = 1000L * 60 * 30; // 30 min
    private long refreshExpiration = 1000L * 60 * 60 * 24 * 7; // 7 days

    @BeforeEach
    void setup() {
        jwtTokenProvider = new JwtTokenProvider(secretKey, accessExpiration, refreshExpiration);
    }

    @Test
    @DisplayName("토큰 생성 테스트")
    void generateToken_Success() {
        // given
        Authentication authentication = new UsernamePasswordAuthenticationToken("user", "",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

        // when
        TokenInfo tokenInfo = jwtTokenProvider.generateToken(authentication);

        // then
        assertThat(tokenInfo).isNotNull();
        assertThat(tokenInfo.getAccessToken()).isNotNull();
        assertThat(tokenInfo.getRefreshToken()).isNotNull();
    }

    @Test
    @DisplayName("토큰 유효성 검증 - 유효한 토큰")
    void validateToken_Valid() {
        // given
        Authentication authentication = new UsernamePasswordAuthenticationToken("user", "",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        TokenInfo tokenInfo = jwtTokenProvider.generateToken(authentication);

        // when
        boolean isValid = jwtTokenProvider.validateToken(tokenInfo.getAccessToken());

        // then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("토큰 유효성 검증 - 만료된 토큰")
    void validateToken_Expired() throws InterruptedException {
        // given
        // 만료 시간이 아주 짧은 Provider 생성 (1ms)
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider(secretKey, 1L, 1L);
        Authentication authentication = new UsernamePasswordAuthenticationToken("user", "",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        TokenInfo tokenInfo = shortLivedProvider.generateToken(authentication);

        // 토큰 만료를 위해 잠시 대기
        Thread.sleep(10);

        // when
        boolean isValid = shortLivedProvider.validateToken(tokenInfo.getAccessToken());

        // then
        assertFalse(isValid, "만료된 토큰은 유효하지 않아야 합니다.");
    }

    @Test
    @DisplayName("토큰 유효성 검증 - 잘못된 시그니처")
    void validateToken_InvalidSignature() {
        // given
        // 다른 키로 서명된 토큰 생성
        String otherKey = "dGhpc2lzYS1keW1teS1rZXktdGhhdC1pcy1kaWZmZXJlbnQtZnJvbS10aGUtb3JpZ2luYWw=";
        byte[] keyBytes = Decoders.BASE64.decode(otherKey);
        Key key = Keys.hmacShaKeyFor(keyBytes);

        String invalidToken = Jwts.builder()
                .setSubject("user")
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // when
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // then
        assertFalse(isValid, "서명이 다른 토큰은 유효하지 않아야 합니다.");
    }
}

package com.foodcom.firstpro.auth.repository;

import com.foodcom.firstpro.auth.domain.RefreshToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RefreshTokenRepositoryTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @AfterEach
    void tearDown() {
        refreshTokenRepository.deleteAll();
    }

    @Test
    @DisplayName("RefreshToken 저장 및 조회 (Redis 연동 테스트)")
    void saveAndFind_Success() {
        // given
        String loginId = "testuser";
        String tokenValue = "refresh-token-value";
        RefreshToken refreshToken = RefreshToken.builder()
                .loginId(loginId)
                .tokenValue(tokenValue)
                .build();

        // when
        refreshTokenRepository.save(refreshToken);
        Optional<RefreshToken> foundToken = refreshTokenRepository.findById(loginId);

        // then
        assertThat(foundToken).isPresent();
        assertThat(foundToken.get().getLoginId()).isEqualTo(loginId);
        assertThat(foundToken.get().getTokenValue()).isEqualTo(tokenValue);
    }

    @Test
    @DisplayName("RefreshToken 삭제 테스트")
    void delete_Success() {
        // given
        String loginId = "testuser";
        RefreshToken refreshToken = RefreshToken.builder()
                .loginId(loginId)
                .tokenValue("refresh-token-value")
                .build();
        refreshTokenRepository.save(refreshToken);

        // when
        refreshTokenRepository.deleteById(loginId);
        Optional<RefreshToken> foundToken = refreshTokenRepository.findById(loginId);

        // then
        assertThat(foundToken).isEmpty();
    }
}

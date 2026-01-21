package com.foodcom.firstpro.auth.repository;

import com.foodcom.firstpro.auth.domain.RefreshToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RefreshTokenRepositoryTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @AfterEach
    void tearDown() {
        refreshTokenRepository.deleteAll();
    }

    @Test
    @DisplayName("RefreshToken 저장 시 만료 시간(TTL)이 정상적으로 설정되는지 확인")
    void save_VerifyTimeToLive() {
        // given
        String loginId = "testuser";
        String tokenValue = "refresh-token-value";
        RefreshToken refreshToken = RefreshToken.builder()
                .loginId(loginId)
                .tokenValue(tokenValue)
                .build();

        // when
        refreshTokenRepository.save(refreshToken);

        // then
        Optional<RefreshToken> foundToken = refreshTokenRepository.findById(loginId);
        assertThat(foundToken).isPresent();
        assertThat(foundToken.get().getTokenValue()).isEqualTo(tokenValue);

        String redisKey = "refreshToken:" + loginId;

        Long expireTime = redisTemplate.getExpire(redisKey);

        assertThat(expireTime).isGreaterThan(0L);
    }
}
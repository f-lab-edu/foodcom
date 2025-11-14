package com.foodcom.firstpro.auth.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@Getter
@Builder
@AllArgsConstructor
@RedisHash(value = "refreshToken", timeToLive = 604800)
public class RefreshToken {

    /**
     * Springframework Id랑 jakarata Id 차이 찾아보기
     */
    @Id
    private String loginId;

    @Indexed
    private String tokenValue;
}

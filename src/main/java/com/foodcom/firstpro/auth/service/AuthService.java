package com.foodcom.firstpro.auth.service;

import com.foodcom.firstpro.auth.domain.RefreshToken;
import com.foodcom.firstpro.auth.dto.TokenInfo;
import com.foodcom.firstpro.auth.repository.RefreshTokenRepository;
import com.foodcom.firstpro.auth.util.JwtTokenProvider;
import com.foodcom.firstpro.domain.member.MemberLoginDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public TokenInfo login(MemberLoginDTO memberLoginDTO) {
        // 1. ID/PW를 기반으로 인증 시도 객체 생성
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(memberLoginDTO.getLoginId(), memberLoginDTO.getPassword());

        // 2. 인증 매니저를 통해 실제 검증 수행 (loadUserByUsername 호출 및 비밀번호 매칭)
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // 3. 인증 정보를 기반으로 JWT 토큰 생성
        TokenInfo tokenInfo = jwtTokenProvider.generateToken(authentication);

        RefreshToken refreshToken = RefreshToken.builder()
                .loginId(authentication.getName())
                .tokenValue(tokenInfo.getRefreshToken())
                .build();

        refreshTokenRepository.save(refreshToken);

        return tokenInfo;
    }
}

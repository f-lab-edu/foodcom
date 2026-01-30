package com.foodcom.firstpro.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodcom.firstpro.auth.domain.RefreshToken;
import com.foodcom.firstpro.auth.dto.TokenInfo;
import com.foodcom.firstpro.auth.repository.RefreshTokenRepository;
import com.foodcom.firstpro.auth.util.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.Cookie;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AuthControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private JwtTokenProvider jwtTokenProvider;

        @MockitoBean
        private RefreshTokenRepository refreshTokenRepository;

        @MockitoBean
        private com.google.cloud.storage.Storage storage;

        @Test
        @DisplayName("토큰 재발급 성공 테스트")
        public void reissueAccessToken_Success() throws Exception {
                // given
                String loginId = "testuser";
                Authentication authentication = new UsernamePasswordAuthenticationToken(loginId, "",
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

                // 유효한 리프레시 토큰 생성
                TokenInfo tokenInfo = jwtTokenProvider.generateToken(authentication);
                String refreshTokenVal = tokenInfo.getRefreshToken();

                // RefreshTokenRepository가 저장된 토큰을 반환하도록 Mocking
                RefreshToken storedToken = RefreshToken.builder()
                                .loginId(loginId)
                                .tokenValue(refreshTokenVal)
                                .build();

                given(refreshTokenRepository.findById(loginId)).willReturn(Optional.of(storedToken));
                given(refreshTokenRepository.save(any(RefreshToken.class)))
                                .willAnswer(invocation -> invocation.getArgument(0));

                // when & then
                mockMvc.perform(post("/auth/reissue")
                                .cookie(new Cookie("refresh_token", refreshTokenVal))
                                .with(csrf())
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.accessToken").exists())
                                .andExpect(cookie().exists("refresh_token"));
        }

        @Test
        @DisplayName("토큰 재발급 실패 - 쿠키 없음 테스트")
        public void reissueAccessToken_NoCookie() throws Exception {
                // when & then
                mockMvc.perform(post("/auth/reissue")
                                .with(csrf())
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("토큰 재발급 실패 - 유효하지 않은 토큰 (401 Unauthorized)")
        public void reissueAccessToken_InvalidToken() throws Exception {
                // given
                String invalidToken = "invalid-token-value";

                // when & then
                mockMvc.perform(post("/auth/reissue")
                                .cookie(new Cookie("refresh_token", invalidToken))
                                .with(csrf())
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isUnauthorized());
        }
}

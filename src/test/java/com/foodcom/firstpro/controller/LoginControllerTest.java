package com.foodcom.firstpro.controller;

import com.foodcom.firstpro.auth.domain.RefreshToken;
import com.foodcom.firstpro.auth.repository.RefreshTokenRepository;
import com.foodcom.firstpro.domain.member.Gender;
import com.foodcom.firstpro.domain.member.Member;
import com.foodcom.firstpro.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    @DisplayName("회원가입 성공 테스트 (DB 저장 확인)")
    public void join_Success() throws Exception {
        // given
        String loginId = "newuser";
        String jsonRequest = "{\"loginId\":\"" + loginId
                + "\",\"password\":\"password\",\"username\":\"New User\",\"age\":20,\"gender\":\"FEMALE\"}";

        // when & then
        mockMvc.perform(post("/members")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));

        // DB 검증
        if (memberRepository.findByLoginId(loginId).isEmpty()) {
            throw new AssertionError("회원가입 후 DB에 유저가 존재하지 않습니다.");
        }
    }

    @Test
    @DisplayName("로그인 성공 테스트 (토큰 발급 확인)")
    public void login_Success() throws Exception {
        // given
        // DB에 유저 미리 생성 (비밀번호 암호화 필수)
        String loginId = "loginuser";
        String rawPassword = "password123";
        Member member = Member.builder()
                .loginId(loginId)
                .password(passwordEncoder.encode(rawPassword))
                .username("Login User")
                .age(30)
                .gender(Gender.MALE)
                .uuid(UUID.randomUUID().toString())
                .build();
        memberRepository.save(member);

        // RefreshTokenRepository mocking (Redis bypass)
        given(refreshTokenRepository.save(any(RefreshToken.class))).willAnswer(invocation -> invocation.getArgument(0));

        String jsonRequest = "{\"loginId\":\"" + loginId + "\",\"password\":\"" + rawPassword + "\"}";

        // when & then
        mockMvc.perform(post("/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(cookie().exists("refresh_token"));
    }

    @Test
    @DisplayName("회원가입 실패 테스트 - 중복된 아이디 (409 Conflict)")
    public void join_DuplicateId() throws Exception {
        // given
        // 미리 유저 생성
        String duplicateId = "duplicateUser";
        Member existingMember = Member.builder()
                .loginId(duplicateId)
                .password("password")
                .username("Existing User")
                .age(20)
                .gender(Gender.FEMALE)
                .uuid(UUID.randomUUID().toString())
                .build();
        memberRepository.save(existingMember);

        // 동일한 ID로 가입 요청
        String jsonRequest = "{\"loginId\":\"" + duplicateId
                + "\",\"password\":\"password\",\"username\":\"New User\",\"age\":20,\"gender\":\"FEMALE\"}";

        // when & then
        mockMvc.perform(post("/members")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isConflict()); // 409
    }

    @Test
    @DisplayName("로그인 실패 테스트 - 비밀번호 불일치 (401 Unauthorized)")
    public void login_Fail() throws Exception {
        // given
        String loginId = "loginuser_fail";
        String correctPassword = "password123";
        Member member = Member.builder()
                .loginId(loginId)
                .password(passwordEncoder.encode(correctPassword))
                .username("Login User")
                .age(30)
                .gender(Gender.MALE)
                .uuid(UUID.randomUUID().toString())
                .build();
        memberRepository.save(member);

        // 틀린 비밀번호 요청
        String jsonRequest = "{\"loginId\":\"" + loginId + "\",\"password\":\"wrongpassword\"}";

        // when & then
        mockMvc.perform(post("/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isUnauthorized()); // 401
    }
}

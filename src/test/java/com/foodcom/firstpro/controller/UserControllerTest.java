package com.foodcom.firstpro.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodcom.firstpro.domain.member.Member;
import com.foodcom.firstpro.domain.member.MemberUpdateDto;
import com.foodcom.firstpro.repository.MemberRepository;
import com.google.cloud.storage.Storage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import com.foodcom.firstpro.domain.member.Gender;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class UserControllerTest {

    @MockitoBean
    private Storage storage;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Member testMember;

    @BeforeEach
    public void setup() {
        testMember = Member.builder()
                .loginId("testuser")
                .password("password")
                .username("Test User")
                .age(25)
                .gender(Gender.MALE)

                .build();
        memberRepository.save(testMember);
    }

    @Test
    @DisplayName("마이페이지 조회 성공 테스트 (DB 조회 확인)")
    @WithMockUser(username = "testuser")
    public void getMyInfo_Success() throws Exception {
        // when & then
        mockMvc.perform(get("/mypage")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Test User"))
                .andExpect(jsonPath("$.loginId").value("testuser"));
    }

    @Test
    @DisplayName("마이페이지 수정 성공 테스트 (DB 변경 확인)")
    @WithMockUser(username = "testuser")
    public void updateMyInfo_Success() throws Exception {
        // given
        String newUsername = "Updated User";
        String jsonRequest = "{\"newName\":\"" + newUsername + "\"}"; // MemberUpdateDto field: newName

        // when & then
        mockMvc.perform(patch("/mypage")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isNoContent());

        // DB 검증
        Member updatedMember = memberRepository.findByLoginId("testuser").orElseThrow();
        if (!updatedMember.getUsername().equals(newUsername)) {
            throw new AssertionError("이름이 수정되지 않았습니다.");
        }
    }
}

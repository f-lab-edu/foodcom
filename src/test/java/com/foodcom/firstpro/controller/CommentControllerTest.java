package com.foodcom.firstpro.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodcom.firstpro.domain.comment.Comment;
import com.foodcom.firstpro.domain.member.Member;
import com.foodcom.firstpro.domain.post.Post;
import com.foodcom.firstpro.repository.CommentRepository;
import com.foodcom.firstpro.repository.MemberRepository;
import com.foodcom.firstpro.repository.PostRepository;
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
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class CommentControllerTest {

    @MockitoBean
    private Storage storage;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PostRepository postRepository;

    private Member testMember;
    private Post testPost;

    @BeforeEach
    public void setup() {
        testMember = Member.builder()
                .loginId("testuser")
                .password("password")
                .username("Test User")
                .age(25)
                .gender(Gender.MALE)
                .uuid(UUID.randomUUID().toString())
                .build();
        memberRepository.save(testMember);

        testPost = Post.builder()
                .title("Test Post")
                .content("Content")
                .member(testMember)
                .build();
        postRepository.save(testPost);
    }

    @Test
    @DisplayName("댓글 생성 성공 테스트 (DB 저장 확인)")
    @WithMockUser(username = "testuser")
    public void createComment_Success() throws Exception {
        // given
        String content = "Nice post!";
        String jsonRequest = "{\"content\":\"" + content + "\"}";

        // when & then
        mockMvc.perform(post("/posts/{postId}/comments", testPost.getId())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isCreated());

        // DB 검증
        boolean exists = commentRepository.findAll().stream()
                .anyMatch(comment -> comment.getContent().equals(content));
        if (!exists) {
            throw new AssertionError("댓글이 DB에 저장되지 않았습니다.");
        }
    }
}

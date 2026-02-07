package com.foodcom.firstpro.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodcom.firstpro.domain.member.Gender;
import com.foodcom.firstpro.domain.member.Member;
import com.foodcom.firstpro.domain.post.Post;
import com.foodcom.firstpro.repository.MemberRepository;
import com.foodcom.firstpro.repository.PostRepository;
import com.foodcom.firstpro.service.StorageService;
import com.foodcom.firstpro.domain.post.PostCreateRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // 테스트 종료 후 DB 롤백
public class PostControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private PostRepository postRepository;

        @Autowired
        private MemberRepository memberRepository;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private StorageService storageService;

        private Member testMember;

        @BeforeEach
        public void setup() {
                // 테스트용 멤버 생성
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
        @DisplayName("게시물 생성 API 성공 테스트 (DB 저장 확인)")
        @WithMockUser(username = "testuser")
        public void createPost_Success() throws Exception {
                // given
                String title = "Integration Title";
                String content = "Integration Content";

                // StorageService Mocking
                given(storageService.uploadFile(any(), anyString())).willReturn("http://dummy-url.com/image.jpg");

                PostCreateRequestDto requestDto = new PostCreateRequestDto(title, content);
                String jsonRequest = objectMapper.writeValueAsString(requestDto);

                MockMultipartFile dataPart = new MockMultipartFile(
                                "data",
                                "",
                                "application/json",
                                jsonRequest.getBytes(StandardCharsets.UTF_8));

                MockMultipartFile imagePart = new MockMultipartFile(
                                "files",
                                "test.jpg",
                                "image/jpeg",
                                "dummy image content".getBytes());

                // when & then
                mockMvc.perform(multipart("/posts")
                                .file(dataPart)
                                .file(imagePart)
                                .with(csrf())
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isCreated())
                                .andExpect(header().exists("Location"));

                // DB 검증
                // 트랜잭션 내이므로 바로 조회 가능
                Post savedPost = postRepository.findAll().stream()
                                .filter(p -> p.getTitle().equals(title))
                                .findFirst()
                                .orElseThrow(() -> new AssertionError("게시물이 DB에 저장되지 않았습니다."));

                // Assertions.assertEquals(savedPost.getContent(), content); // JUnit Assertions
                // import 필요
        }

        @Test
        @DisplayName("게시물 상세 조회 성공 테스트 (DB 조회 확인)")
        @WithMockUser(username = "testuser")
        public void viewPost_Success() throws Exception {
                // given
                // DB에 직접 데이터 주입
                Post post = Post.builder()
                                .title("View Test Title")
                                .content("View Test Content")
                                .member(testMember)
                                .build();
                // prePersist에서 UUID 생성되므로 save 후 확인
                postRepository.save(post);
                Long postId = post.getId();

                // when & then
                mockMvc.perform(get("/posts/{postId}", postId)
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.title").value("View Test Title"))
                                .andExpect(jsonPath("$.userName").value("Test User"));
        }

        @Test
        @DisplayName("게시물 수정 성공 테스트 (DB 변경 확인)")
        @WithMockUser(username = "testuser")
        public void updatePost_Success() throws Exception {
                // given
                Post post = Post.builder()
                                .title("Original Title")
                                .content("Original Content")
                                .member(testMember)
                                .build();
                postRepository.save(post);
                Long postId = post.getId();

                String updatedTitle = "Updated Title";
                String updatedContent = "Updated Content";
                String jsonRequest = String.format("{\"title\":\"%s\", \"content\":\"%s\"}", updatedTitle,
                                updatedContent);

                MockMultipartFile dataPart = new MockMultipartFile(
                                "data", "", "application/json", jsonRequest.getBytes(StandardCharsets.UTF_8));

                // when & then
                mockMvc.perform(multipart("/posts/{postId}", postId)
                                .file(dataPart)
                                .with(request -> {
                                        request.setMethod("PATCH");
                                        return request;
                                })
                                .with(csrf())
                                .contentType(MediaType.MULTIPART_FORM_DATA))
                                .andExpect(status().isOk());

                // DB 검증
                Post updatedPost = postRepository.findById(post.getId()).orElseThrow();
                if (!updatedPost.getTitle().equals(updatedTitle)) {
                        throw new AssertionError("제목이 수정되지 않았습니다.");
                }
        }

        @Test
        @DisplayName("게시물 삭제 성공 테스트 (DB 삭제 확인)")
        @WithMockUser(username = "testuser")
        public void deletePost_Success() throws Exception {
                // given
                Post post = Post.builder()
                                .title("Delete Title")
                                .content("Delete Content")
                                .member(testMember)
                                .build();
                postRepository.save(post);
                Long postId = post.getId();

                // Mock StorageService delete (if applicable)

                // when & then
                mockMvc.perform(delete("/posts/{postId}", postId)
                                .with(csrf()))
                                .andExpect(status().isNoContent());

                // DB 검증
                if (postRepository.existsById(post.getId())) {
                        throw new AssertionError("게시물이 삭제되지 않았습니다.");
                }
        }

        @Test
        @DisplayName("게시물 목록 조회 성공 테스트")
        @WithMockUser(username = "testuser")
        public void getPostList_Success() throws Exception {
                // given
                for (int i = 1; i <= 5; i++) {
                        postRepository.save(Post.builder()
                                        .title("Post " + i)
                                        .content("Content " + i)
                                        .member(testMember)
                                        .build());
                }

                // when & then
                mockMvc.perform(get("/posts")
                                .param("page", "1")
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.postList.length()").value(5));
        }

        @Test
        @DisplayName("게시물 수정 실패 테스트 - 권한 없음 (403 Forbidden)")
        @WithMockUser(username = "otheruser")
        public void updatePost_AccessDenied() throws Exception {
                // given
                Post post = Post.builder()
                                .title("Original Title")
                                .content("Original Content")
                                .member(testMember) // Author is "testuser"
                                .build();
                postRepository.save(post);
                Long postId = post.getId();

                String jsonRequest = "{\"title\":\"Updated\", \"content\":\"Updated content\"}";
                MockMultipartFile dataPart = new MockMultipartFile(
                                "data", "", "application/json", jsonRequest.getBytes(StandardCharsets.UTF_8));

                // when & then
                mockMvc.perform(multipart("/posts/{postId}", postId)
                                .file(dataPart)
                                .with(request -> {
                                        request.setMethod("PATCH");
                                        return request;
                                })
                                .with(csrf())
                                .contentType(MediaType.MULTIPART_FORM_DATA))
                                .andExpect(status().isForbidden()); // 403
        }

        @Test
        @DisplayName("게시물 삭제 실패 테스트 - 권한 없음 (403 Forbidden)")
        @WithMockUser(username = "otheruser")
        public void deletePost_AccessDenied() throws Exception {
                // given
                Post post = Post.builder()
                                .title("Delete Title")
                                .content("Delete Content")
                                .member(testMember)
                                .build();
                postRepository.save(post);

                // when & then
                mockMvc.perform(delete("/posts/{postId}", post.getId())
                                .with(csrf()))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("게시물 조회 실패 테스트 - 존재하지 않는 게시물 (404 Not Found)")
        @WithMockUser(username = "testuser")
        public void viewPost_NotFound() throws Exception {
                // given
                Long invalidId = 999999L;

                // when & then
                mockMvc.perform(get("/posts/{postId}", invalidId)
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("게시물 생성 실패 테스트 - 유효성 검사 실패 (400 Bad Request)")
        @WithMockUser(username = "testuser")
        public void createPost_ValidationFail() throws Exception {
                // given
                // 제목과 내용이 비어있는 요청
                PostCreateRequestDto requestDto = new PostCreateRequestDto("", "");
                String jsonRequest = objectMapper.writeValueAsString(requestDto);

                MockMultipartFile dataPart = new MockMultipartFile(
                                "data", "", "application/json", jsonRequest.getBytes(StandardCharsets.UTF_8));

                // when & then
                mockMvc.perform(multipart("/posts")
                                .file(dataPart)
                                .with(csrf())
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isBadRequest());
        }
}

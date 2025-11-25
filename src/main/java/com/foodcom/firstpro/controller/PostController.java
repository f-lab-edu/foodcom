package com.foodcom.firstpro.controller;

import com.foodcom.firstpro.domain.post.Post;
import com.foodcom.firstpro.domain.post.PostRequestDto;
import com.foodcom.firstpro.service.PostService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    /**
     * 인증된 사용자가 새로운 게시물과 이미지 파일을 생성합니다.
     * 요청은 multipart/form-data 형태로 이루어지며, 사용자 ID는 JWT를 통해 SecurityContext에서 가져옵니다.
     * * @param requestDto 게시물 제목, 내용 (JSON/텍스트 파트)
     * @param imageFiles 첨부된 이미지 파일 리스트 (파일 파트)
     * @return 생성된 Post 정보와 HTTP 상태 코드
     */
    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Void> createPost(
            @Parameter(description = "게시물 제목 및 내용 (JSON 형식)", required = true)
            @RequestPart("data") PostRequestDto requestDto,

            @Parameter(description = "첨부할 이미지 파일 리스트", required = false)
            @RequestPart(value = "files", required = false) List<MultipartFile> imageFiles
    ) {
        try {
            log.info("createPost시작");
            Post newPost = postService.createPost(
                    requestDto.getTitle(),
                    requestDto.getContent(),
                    imageFiles
            );
            log.info("postService 종료");

            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(newPost.getId())
                    .toUri();

            // 2. HTTP 201 Created 상태 코드와 Location 헤더를 반환합니다. (본문 없음)
            return ResponseEntity.created(location).build();
        } catch (IOException e) {
            throw new RuntimeException("이미지 업로드 중 오류가 발생했습니다: " + e.getMessage(), e);
        } catch (SecurityException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }
}
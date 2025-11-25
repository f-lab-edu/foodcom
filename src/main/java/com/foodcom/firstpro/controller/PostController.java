package com.foodcom.firstpro.controller;

import com.foodcom.firstpro.controller.advice.GlobalExceptionHandler;
import com.foodcom.firstpro.domain.post.Post;
import com.foodcom.firstpro.domain.post.PostRequestDto;
import com.foodcom.firstpro.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.List;

@Slf4j
@Tag(name = "게시물 관리", description = "게시물 생성, 조회, 수정, 삭제 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;


    @Operation(summary = "새 게시물 및 이미지 생성",
            description = "인증된 사용자가 제목, 내용, 첨부 파일(선택)을 포함하여 새 게시물을 생성합니다. 성공 시 Location 헤더에 새 리소스 URI를 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "게시물 생성 성공. Location 헤더 확인 posts/id 호출",
                    content = @Content(schema = @Schema(implementation = Void.class))),

            @ApiResponse(responseCode = "401", description = "인증 실패 (유효하지 않은 토큰 또는 토큰 없음)",
                    content = @Content(
                            schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "인증 실패 예시",
                                    value = "{\"code\": \"인증 실패\", \"message\": \"Access Token이 유효하지 않거나 필요합니다.\"}"
                            )
                    )),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터",
                    content = @Content(
                            schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "유효성 검사 실패 예시",
                                    value = "{\"code\": \"Validation Failed\", \"message\": {\"title\": \"제목은 필수로 입력해야 합니다.\"}}"
                            )
                    )),
            @ApiResponse(responseCode = "500", description = "서버 오류 (예: GCS 업로드 실패 등)",
                    content = @Content(
                            schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "서버 오류 예시",
                                    value = "{\"code\": \"Internal Server Error\", \"message\": \"서버 처리 중 예상치 못한 오류가 발생했습니다.\"}"
                            )
                    ))
    })
    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Void> createPost(
            @Parameter(
                    description = "게시물 제목 및 내용 (JSON 형식)", required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PostRequestDto.class)
                    )
            )
            @RequestPart("data") @Valid PostRequestDto requestDto,

            @Parameter(
                    description = "첨부할 이미지 파일 리스트", required = false,
                    array = @ArraySchema(
                            schema = @Schema(type = "string", format = "binary")
                    )
            )
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
package com.foodcom.firstpro.controller;

import com.foodcom.firstpro.controller.advice.GlobalExceptionHandler;
import com.foodcom.firstpro.domain.post.Post;
import com.foodcom.firstpro.domain.post.PostCreateRequestDto;
import com.foodcom.firstpro.domain.post.PostResponseDto;
import com.foodcom.firstpro.domain.post.PostUpdateRequestDto;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.nio.file.AccessDeniedException;
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
                            schema = @Schema(implementation = PostCreateRequestDto.class)
                    )
            )
            @RequestPart("data") @Valid PostCreateRequestDto requestDto,

            @Parameter(
                    description = "첨부할 이미지 파일 리스트", required = false,
                    array = @ArraySchema(
                            schema = @Schema(type = "string", format = "binary")
                    )
            )
            @RequestPart(value = "files", required = false) List<MultipartFile> imageFiles
    ) {
        try {
            Post newPost = postService.createPost(
                    requestDto.getTitle(),
                    requestDto.getContent(),
                    imageFiles
            );

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

    @Operation(summary = "게시물 상세 조회", description = "UUID를 통해 특정 게시물의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            // 200 OK
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = PostResponseDto.class))),

            // 404 Not Found (게시물이 없을 때)
            @ApiResponse(responseCode = "404", description = "게시물을 찾을 수 없음 (잘못된 UUID)",
                    content = @Content(
                            schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "게시물 없음",
                                    summary = "존재하지 않는 UUID로 조회 시",
                                    value = "{\"code\": \"Resource Not Found\", \"message\": \"게시물을 찾을 수 없습니다. Uuid = 550e8400-e29b-41d4-a716-446655440000\"}"
                            )
                    )),

            // 500 Internal Server Error
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(
                            schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "서버 에러 예시",
                                    value = "{\"code\": \"Internal Server Error\", \"message\": \"서버 처리 중 예상치 못한 오류가 발생했습니다.\"}"
                            )
                    ))
    })
    @GetMapping("/{postUuid}")
    public ResponseEntity<PostResponseDto> viewPost(
            @Parameter(description = "게시물 UUID", required = true)
            @PathVariable("postUuid") String postUuid
    ) {
        PostResponseDto postInfo = postService.getPostInfo(postUuid);
        return ResponseEntity.ok(postInfo);
    }

    @Operation(summary = "게시물 수정", description = "제목, 내용을 수정하고, 기존 이미지를 삭제하거나 새 이미지를 추가합니다.")
    @ApiResponses(value = {
            // 200 OK
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = Void.class))),

            // 400 Bad Request
            @ApiResponse(responseCode = "400", description = "입력값 유효성 검사 실패 (JSON 형식 오류 등)",
                    content = @Content(
                            schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "유효성 검사 실패",
                                    value = "{\"code\": \"Validation Failed\", \"message\": {\"title\": \"제목은 100자 이하여야 합니다.\"}}"
                            )
                    )),

            // 401 Unauthorized
            @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음)",
                    content = @Content(
                            schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "인증 실패",
                                    value = "{\"code\": \"인증 실패\", \"message\": \"Access Token이 유효하지 않거나 필요합니다.\"}"
                            )
                    )),

            // 403 Forbidden (작성자가 아님)
            @ApiResponse(responseCode = "403", description = "권한 없음 (작성자만 수정 가능)",
                    content = @Content(
                            schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "권한 없음",
                                    value = "{\"code\": \"권한 없음\", \"message\": \"작성자만 게시물을 수정할 수 있습니다.\"}"
                            )
                    )),

            // 404 Not Found
            @ApiResponse(responseCode = "404", description = "게시물을 찾을 수 없음",
                    content = @Content(
                            schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "게시물 없음",
                                    value = "{\"code\": \"Resource Not Found\", \"message\": \"게시물을 찾을 수 없습니다.\"}"
                            )
                    )),

            // 500 Internal Server Error
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 (파일 업로드 실패 등)",
                    content = @Content(
                            schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "파일 오류",
                                    value = "{\"code\": \"File Upload Error\", \"message\": \"파일 처리 중 오류가 발생했습니다.\"}"
                            )
                    ))
    })
    @PatchMapping(value = "/{postUuid}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updatePost(
            @PathVariable("postUuid") String postUuid,

            @Parameter(
                    description = "수정할 데이터 (JSON)",
                    required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            )
            @RequestPart("data") @Valid PostUpdateRequestDto updateDto,

            // 2. 파일 파트
            @Parameter(
                    description = "새로 추가할 이미지 파일 리스트",
                    required = false
            )
            @RequestPart(value = "files", required = false) List<MultipartFile> newFiles,

            @AuthenticationPrincipal UserDetails userDetails
    ) throws IOException {
        postService.updatePost(postUuid, updateDto, newFiles, userDetails.getUsername());

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "게시물 삭제", description = "특정 게시물과 연관된 모든 이미지, 댓글을 영구 삭제합니다.")
    @ApiResponses(value = {
            // 204 No Content (삭제 성공 시 본문 없음)
            @ApiResponse(responseCode = "204", description = "삭제 성공"),

            // 401 Unauthorized
            @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음)",
                    content = @Content(
                            schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "인증 실패",
                                    value = "{\"code\": \"인증 실패\", \"message\": \"Access Token이 유효하지 않거나 필요합니다.\"}"
                            )
                    )),

            // 403 Forbidden
            @ApiResponse(responseCode = "403", description = "권한 없음 (작성자만 삭제 가능)",
                    content = @Content(
                            schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "권한 없음",
                                    value = "{\"code\": \"권한 없음\", \"message\": \"작성자만 게시물을 삭제할 수 있습니다.\"}"
                            )
                    )),

            // 404 Not Found
            @ApiResponse(responseCode = "404", description = "게시물을 찾을 수 없음",
                    content = @Content(
                            schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "게시물 없음",
                                    value = "{\"code\": \"Resource Not Found\", \"message\": \"게시물을 찾을 수 없습니다.\"}"
                            )
                    ))
    })
    @DeleteMapping("/{postUuid}")
    public ResponseEntity<Void> deletePost(
            @Parameter(description = "삭제할 게시물 UUID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable("postUuid") String postUuid,

            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetails userDetails
    ) throws AccessDeniedException {
        postService.deletePost(postUuid, userDetails.getUsername());

        return ResponseEntity.noContent().build();
    }
}
package com.foodcom.firstpro.controller;

import com.foodcom.firstpro.controller.advice.GlobalExceptionHandler;
import com.foodcom.firstpro.domain.comment.CommentCreateDto;
import com.foodcom.firstpro.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "댓글 관리", description = "댓글 생성, 수정, 삭제 API")
@RestController
@RequiredArgsConstructor
public class CommentController {

        private final CommentService commentService;

        @Operation(summary = "댓글 작성", description = "특정 게시물(UUID)에 새로운 댓글을 등록합니다.")
        @ApiResponses(value = {
                        // 201 Created
                        @ApiResponse(responseCode = "201", description = "댓글 작성 성공", content = @Content(schema = @Schema(implementation = Void.class))),

                        // 400 Bad Request (유효성 검사 실패)
                        @ApiResponse(responseCode = "400", description = "입력값 유효성 검사 실패 (내용 누락, 길이 초과 등)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class), examples = @ExampleObject(name = "유효성 검사 실패 예시", summary = "댓글 내용이 비어있거나 제한을 넘은 경우", value = "{\n"
                                        +
                                        "  \"code\": \"Validation Failed\",\n" +
                                        "  \"message\": {\n" +
                                        "    \"content\": \"댓글 내용은 필수입니다.\"\n" +
                                        "  }\n" +
                                        "}"))),

                        // 401 Unauthorized (인증 실패)
                        @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음/만료)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class), examples = @ExampleObject(name = "인증 실패 예시", value = "{\n"
                                        +
                                        "  \"code\": \"인증 실패\",\n" +
                                        "  \"message\": \"Access Token이 유효하지 않거나 필요합니다.\"\n" +
                                        "}"))),

                        // 404 Not Found (게시물 없음)
                        @ApiResponse(responseCode = "404", description = "대상 게시물을 찾을 수 없음", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class), examples = @ExampleObject(name = "게시물 없음 예시", value = "{\n"
                                        +
                                        "  \"code\": \"Resource Not Found\",\n" +
                                        "  \"message\": \"게시물을 찾을 수 없습니다.\"\n" +
                                        "}")))
        })
        @PostMapping("/posts/{postId}/comments")
        public ResponseEntity<Void> createComment(
                        @Parameter(description = "게시물 ID", required = true) @PathVariable("postId") Long postId,

                        @Valid @RequestBody CommentCreateDto commentCreateDto,

                        @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
                commentService.createComment(postId, commentCreateDto, userDetails.getUsername());

                return ResponseEntity.status(HttpStatus.CREATED).build();
        }
}

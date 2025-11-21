package com.foodcom.firstpro.controller;

import com.foodcom.firstpro.controller.advice.GlobalExceptionHandler;
import com.foodcom.firstpro.domain.member.MemberUpdateDto;
import com.foodcom.firstpro.domain.post.MyPageResponse;
import com.foodcom.firstpro.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject; // 💡 추가됨
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/mypage")
@Tag(name = "User API", description = "마이페이지 정보 조회 및 수정")
public class UserController {

    private final UserService userService;

    @Operation(summary = "마이페이지 정보 조회", description = "로그인된 사용자의 상세 정보를 조회합니다.")
    @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = MyPageResponse.class))
    )
    @ApiResponse(
            responseCode = "401",
            description = "인증 실패 (토큰 없음 또는 만료)",
            content = @Content(
                    schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                    examples = @ExampleObject( // 💡 JWT 인증 실패 예시 추가
                            name = "JWT 인증 실패 예시",
                            value = "{\n" +
                                    "  \"code\": \"인증 실패\",\n" +
                                    "  \"message\": \"Access Token이 유효하지 않거나 필요합니다.\"\n" +
                                    "}"
                    )
            )
    )
    @GetMapping("")
    public ResponseEntity<MyPageResponse> getMyInfo(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userId = userDetails.getUsername();
        MyPageResponse response = userService.getMyInfo(userId);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "마이페이지 정보 수정", description = "로그인된 사용자의 정보를 부분적으로 수정합니다.")
    @ApiResponse(
            responseCode = "204",
            description = "수정 성공 (응답 본문 없음)"
    )
    @ApiResponse(
            responseCode = "400",
            description = "입력값 유효성 검사 실패",
            content = @Content(
                    schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                    examples = @ExampleObject( // 💡 유효성 검사 실패 예시 추가
                            name = "유효성 검사 실패 예시",
                            value = "{\n" +
                                    "  \"code\": \"Validation Failed\",\n" +
                                    "  \"message\": {\n" +
                                    "    \"password\": \"비밀번호는 8자 이상 20자 이하로 입력해야 합니다.\",\n" +
                                    "  }\n" +
                                    "}"
                    )
            )
    )
    @ApiResponse(
            responseCode = "401",
            description = "인증 실패 (토큰 없음 또는 만료)",
            content = @Content(
                    schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                    examples = @ExampleObject( // 💡 JWT 인증 실패 예시 추가
                            name = "JWT 인증 실패 예시",
                            value = "{\n" +
                                    "  \"code\": \"인증 실패\",\n" +
                                    "  \"message\": \"Access Token이 유효하지 않거나 필요합니다.\"\n" +
                                    "}"
                    )
            )
    )
    @PatchMapping("/edit")
    public ResponseEntity<Void> updateMyInfo(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid MemberUpdateDto updateDto
    ) {
        String userId = userDetails.getUsername();

        userService.updateMyInfo(userId, updateDto);

        return ResponseEntity.noContent().build();
    }
}
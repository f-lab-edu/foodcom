package com.foodcom.firstpro.controller;

import com.foodcom.firstpro.auth.dto.AccessTokenResponse;
import com.foodcom.firstpro.auth.dto.TokenInfo;
import com.foodcom.firstpro.auth.service.AuthService;
import com.foodcom.firstpro.auth.util.CookieUtil;
import com.foodcom.firstpro.domain.member.MemberJoinDTO;
import com.foodcom.firstpro.domain.member.MemberLoginDTO;
import com.foodcom.firstpro.service.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.foodcom.firstpro.controller.advice.GlobalExceptionHandler;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@Tag(name = "Login API", description = "회원 가입 및 로그인")
public class LoginController {

    private final LoginService loginService;
    private final AuthService authService;

    @Operation(summary = "회원 가입", description = "회원가입을 하고 완료하면 /login으로 경로 설정")
    @ApiResponse(
            responseCode = "201",
            description = "회원 가입 성공. Location 헤더에 자원 위치(/login)가 포함됨."
    )
    @ApiResponse(
            responseCode = "400",
            description = "입력값 유효성 검사 실패 (예: 아이디 길이 오류, 필수값 누락 등)",
            content = @Content(
                    schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                    examples = @ExampleObject( // 💡 이 부분 추가
                            name = "유효성 검사 실패 예시",
                            value = "{\n" +
                                    "  \"code\": \"Validation Failed\",\n" +
                                    "  \"message\": {\n" +
                                    "    \"loginId\": \"아이디는 5자 이상 20자 이하로 입력해야 합니다.\",\n" +
                                    "  }\n" +
                                    "}"
                    )
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "아이디 중복 (이미 사용 중인 아이디입니다.)",
            content = @Content(
                    schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                    examples = @ExampleObject( // 💡 이 부분 추가
                            name = "중복 예시",
                            value = "{\n" +
                                    "  \"code\": \"중복되는 아이디입니다.\",\n" +
                                    "  \"message\": \"아이디 'existing_id'는 이미 사용 중입니다.\"\n" +
                                    "}"
                    )
            )
    )
    @PostMapping("/members")
    public ResponseEntity<Void> join(@Valid @RequestBody MemberJoinDTO memberJoinDTO) {

        loginService.join(memberJoinDTO);

        return ResponseEntity
                .created(URI.create("/login"))
                .build();
    }


    @Operation(summary = "로그인", description = "로그인하고, refresh token 쿠키 전송 + access token은 헤더에 전송")
    @PostMapping("/login")
    @ApiResponse(
            responseCode = "200",
            description = "로그인 성공. Access Token이 본문에 반환됨.",
            content = @Content(
                    schema = @Schema(implementation = AccessTokenResponse.class),
                    examples = @ExampleObject( // 💡 이 부분 추가
                            name = "로그인 성공 예시",
                            value = "{\n" +
                                    "  \"grantType\": \"Bearer\",\n" +
                                    "  \"accessToken\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\"\n" +
                                    "}"
                    )
            )
    )
    @ApiResponse(
            responseCode = "401",
            description = "인증 실패 (아이디 또는 비밀번호 불일치)",
            content = @Content(
                    schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                    examples = @ExampleObject( // 💡 이 부분 추가
                            name = "인증 실패 예시",
                            value = "{\n" +
                                    "  \"code\": \"인증 실패\",\n" +
                                    "  \"message\": \"아이디 또는 비밀번호가 일치하지 않습니다.\"\n" +
                                    "}"
                    )
            )
    )
    public ResponseEntity<AccessTokenResponse> login(
            @Valid @RequestBody MemberLoginDTO memberLoginDTO,
            HttpServletResponse response) {

        TokenInfo tokenInfo = authService.login(memberLoginDTO);

        int refreshTokenDuration = 7 * 24 * 60 * 60;

        CookieUtil.addCookie(
                response,
                "refreshToken",
                tokenInfo.getRefreshToken(),
                refreshTokenDuration,
                true,  // HttpOnly: JS 접근 불가 (XSS 방어)
                false  // HTTPS쓰면 true로 바꾸기
        );

        AccessTokenResponse accessTokenResponse = new AccessTokenResponse(
                tokenInfo.getGrantType(),
                tokenInfo.getAccessToken()
        );

        return ResponseEntity.ok(accessTokenResponse);
    }
}

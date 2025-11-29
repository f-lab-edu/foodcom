package com.foodcom.firstpro.controller;


import com.foodcom.firstpro.auth.dto.TokenInfo;
import com.foodcom.firstpro.auth.exception.TokenException;
import com.foodcom.firstpro.auth.service.AuthService;
import com.foodcom.firstpro.auth.util.CookieUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "인증 관리", description = "로그인 및 토큰 재발급 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;


    @Value("${jwt.refresh.expiration}")
    private int refreshTokenMaxAge;

    @Operation(summary = "Access Token 재발급",
            description = "브라우저에 저장된 Refresh Token 쿠키를 사용하여 새로운 Access Token 쌍을 발급받고 Refresh Token 쿠키를 갱신합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "토큰 재발급 및 갱신 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패 (Refresh Token 만료/유효하지 않음)")
    })
    @PostMapping("/reissue")
    public ResponseEntity<TokenInfo> reissueAccessToken(
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response) {log.info(">>>> [Reissue 요청 진입] refreshToken 값: {}", refreshToken);

        if (refreshToken == null) {
            throw new TokenException("Refresh Token 쿠키가 존재하지 않습니다. 재로그인이 필요합니다.");
        }

        TokenInfo newTokenInfo = authService.reissue(refreshToken);

        CookieUtil.addCookie(
                response,
                "refresh_token",
                newTokenInfo.getRefreshToken(),
                this.refreshTokenMaxAge,
                true, // HttpOnly
                false // TODO: HTTPS 사용 시 true로 변경
        );

        return ResponseEntity.ok(newTokenInfo);
    }
}

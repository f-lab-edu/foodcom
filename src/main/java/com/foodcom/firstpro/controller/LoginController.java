package com.foodcom.firstpro.controller;

import com.foodcom.firstpro.auth.dto.AccessTokenResponse;
import com.foodcom.firstpro.auth.dto.TokenInfo;
import com.foodcom.firstpro.auth.service.AuthService;
import com.foodcom.firstpro.auth.util.CookieUtil;
import com.foodcom.firstpro.domain.member.MemberJoinDTO;
import com.foodcom.firstpro.domain.member.MemberLoginDTO;
import com.foodcom.firstpro.service.LoginService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;
    private final AuthService authService;

    @PostMapping("/members")
    public ResponseEntity<Void> join(@Valid @RequestBody MemberJoinDTO memberJoinDTO) {

        String memberUuid = loginService.join(memberJoinDTO);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{uuid}")
                .buildAndExpand(memberUuid)
                .toUri();

        return ResponseEntity
                .created(location)
                .build();
    }

    @PostMapping("/login")
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

package com.foodcom.firstpro.controller;

import com.foodcom.firstpro.domain.member.MemberJoinDTO;
import com.foodcom.firstpro.service.LoginService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;


    @PostMapping("/members")
    public ResponseEntity<Map<String,Long>> join(@Valid @RequestBody MemberJoinDTO memberJoinDTO) {

        Long joinId = loginService.join(memberJoinDTO);
        //  일단 회원가입 후에 구체적 url을 정하지 않아 임시로 정함
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(joinId)
                .toUri();

        Map<String, Long> responseBody = new HashMap<>();
        responseBody.put("id", joinId);

        //201 Created 상태 코드와 Body, Location Header를 포함한 ResponseEntity 반환
        return ResponseEntity
                .created(location)
                .body(responseBody);
    }
}

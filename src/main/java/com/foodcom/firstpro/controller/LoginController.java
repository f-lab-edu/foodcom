package com.foodcom.firstpro.controller;

import com.foodcom.firstpro.domain.member.MemberJoinDTO;
import com.foodcom.firstpro.domain.member.MemberLoginDTO;
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
    public ResponseEntity<Map<String, Long>> join(@Valid @RequestBody MemberJoinDTO memberJoinDTO) {

        log.info("회원가입 Controller 호출");
        Long id = loginService.join(memberJoinDTO);
        //  일단 회원가입 후에 구체적 url을 정하지 않아 임시로 정함
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                /**
                 * 아마 UUID로 변환? 할 예정
                 */
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();

        Map<String, Long> responseBody = new HashMap<>();
        responseBody.put("id", id);

        //201 Created
        return ResponseEntity
                .created(location)
                .body(responseBody);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String,Long>> login(@Valid @RequestBody MemberLoginDTO memberLoginDTO) {
        HashMap<String, Long> responseBody = new HashMap<>();

        Long id = loginService.login(memberLoginDTO);
        //URI 아직 생각 X, 추후에 추가해주기
        responseBody.put("id", id);

        return ResponseEntity.ok(responseBody);
    }
}

package com.foodcom.firstpro.controller;

import com.foodcom.firstpro.domain.member.Member;
import com.foodcom.firstpro.domain.member.MemberLoginDTO;
import com.foodcom.firstpro.service.LoginService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute MemberLoginDTO memberLoginDTO,
                        BindingResult bindingResult,
                        HttpServletResponse response) {
        //Controller에서 1차 필터링(아이디, 비번 Blank 예외 처리)
        if (bindingResult.hasErrors()) {
            log.warn("Bean Validation 오류 {}", bindingResult.getAllErrors());
            //로그인 폼으로 다시 return
            return "login/loginForm";
        }

        Member member=
    }
}

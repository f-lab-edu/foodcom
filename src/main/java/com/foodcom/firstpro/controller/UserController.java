package com.foodcom.firstpro.controller;

import com.foodcom.firstpro.domain.post.MyPageResponse;
import com.foodcom.firstpro.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class UserController {

    private final UserService userService;

    @GetMapping("")
    public ResponseEntity<MyPageResponse> getMyInfo(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userId = userDetails.getUsername();
        MyPageResponse response = userService.getMyInfo(userId);

        return ResponseEntity.ok(response);
    }

//    @GetMapping("/edit")
//    public ResponseEntity getEditForm() {
//
//    }
//
//    @PostMapping("/edit")
//    public ResponseEntity updateMyInfo() {
//
//    }
}

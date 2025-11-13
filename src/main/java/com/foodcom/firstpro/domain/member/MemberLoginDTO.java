package com.foodcom.firstpro.domain.member;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MemberLoginDTO {
    @NotBlank(message = "아이디는 필수 입력 값입니다.")
    private String loginId;
    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    private String password;
}

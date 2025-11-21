package com.foodcom.firstpro.domain.member;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MemberLoginDTO {

    @Schema(description = "아이디 입력", example = "testuser123")
    @NotBlank(message = "아이디는 필수 입력 값입니다.")
    private String loginId;

    @Schema(description = "비밀번호 입력",example = "password!@#1234")
    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    private String password;
}

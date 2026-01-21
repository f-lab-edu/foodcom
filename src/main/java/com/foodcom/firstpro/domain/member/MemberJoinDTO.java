package com.foodcom.firstpro.domain.member;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MemberJoinDTO {

    @NotBlank(message = "아이디는 필수 입력 값입니다.")
    @Size(min = 5, max = 20, message = "아이디는 5자 이상 20자 이하로 입력해야 합니다.")
    @Schema(description = "로그인 시 사용할 아이디", example = "testuser123")
    private String loginId;

    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해야 합니다.")
    @Schema(description = "비밀번호 (8자 이상)", example = "password!@#1234")
    private String password;

    @NotBlank(message = "이름을 입력해주세요.")
    @Schema(description = "사용자 실명 또는 닉네임", example = "홍길동")
    private String username;

    @NotNull(message = "성별을 선택해주세요.")
    @Schema(description = "성별", example = "MALE")
    private Gender gender;

    @NotNull(message = "나이를 설정해주세요.")
    @Schema(description = "사용자 나이", example = "25")
    private Integer age;
}

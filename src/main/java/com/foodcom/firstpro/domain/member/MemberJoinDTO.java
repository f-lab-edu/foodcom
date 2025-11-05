package com.foodcom.firstpro.domain.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class MemberJoinDTO {

    @NotBlank(message = "아이디는 필수 입력 값입니다.")
    private String loginId;
    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    private String password;
    @NotBlank(message = "이름을 입력해주세요.")
    private String username;
    @NotBlank(message = "성별을 선택해주세요.")
    private Gender gender;
    @NotBlank(message = "나이를 설정해주세요.")
    private Integer age;
}

package com.foodcom.firstpro.domain.member;

import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class MemberUpdateDto {

    private String newName;
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해야 합니다.")
    private String newPassword;
    private Gender gender;
    private Integer age;
}

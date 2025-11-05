package com.foodcom.firstpro.domain.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class MemberJoinDTO {

    @NotBlank
    private String loginId;
    @NotBlank
    private String password;
    @NotBlank
    private String username;
    @NotBlank
    private Gender gender;
    @NotBlank
    private Integer age;
}

package com.foodcom.firstpro.domain.member;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class MemberJoinDTO {

    @NotEmpty
    private String loginId;
    @NotEmpty
    private String password;
    @NotEmpty
    private String username;
    @NotEmpty
    private Gender gender;
    @NotEmpty
    private Integer age;
}

package com.foodcom.firstpro.domain.member;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class MemberLoginDTO {

    private String loginId;
    private String password;
}

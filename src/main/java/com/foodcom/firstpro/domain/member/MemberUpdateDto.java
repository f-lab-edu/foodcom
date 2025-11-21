package com.foodcom.firstpro.domain.member;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class MemberUpdateDto {

    @Schema(description = "수정할 이름", example = "김민수")
    private String newName;

    @Schema(description = "새 비밀번호(수정시에만 설정) 8~20자", example = "example123")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해야 합니다.")
    private String newPassword;

    @Schema(description = "성별")
    private Gender gender;

    @Schema(description = "나이")
    private Integer age;
}

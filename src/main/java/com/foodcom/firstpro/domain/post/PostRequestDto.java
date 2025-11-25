package com.foodcom.firstpro.domain.post;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "게시물 생성 요청 DTO")
public class PostRequestDto {

    @NotBlank(message = "제목은 필수로 입력해야 합니다.")
    @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다.")
    @Schema(description = "게시물 제목", example = "새로운 맛집을 소개합니다", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotBlank(message = "내용은 필수로 입력해야 합니다.")
    @Schema(description = "게시물 내용", example = "맛있어요", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;
}
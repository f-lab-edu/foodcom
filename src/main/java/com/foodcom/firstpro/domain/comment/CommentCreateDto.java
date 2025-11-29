package com.foodcom.firstpro.domain.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "댓글 작성 요청 DTO")
@Getter
@NoArgsConstructor
public class CommentCreateDto {

    @Schema(
            description = "작성할 댓글 내용",
            example = "맛있겠어요~~",
            minLength = 1,
            maxLength = 300
    )
    @NotBlank(message = "댓글 내용은 필수입니다.")
    @Size(min = 1, max = 300, message = "댓글은 300자 이하로 작성해주세요.")
    private String content;
}

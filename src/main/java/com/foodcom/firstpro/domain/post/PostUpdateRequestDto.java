package com.foodcom.firstpro.domain.post;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@Schema(description = "게시물 수정 요청 DTO (PATCH)")
public class PostUpdateRequestDto {

    @Schema(
            description = "수정할 제목 (null 또는 생략 시 기존 제목 유지)",
            example = "정말 맛있는 파스타 레시피"
    )
    private String title;

    @Schema(
            description = "수정할 내용 (null 또는 생략 시 기존 내용 유지)",
            example = "재료 목록에 마늘을 추가했습니다."
    )
    private String content;

    @Schema(
            description = "삭제할 기존 이미지의 ID(PK) 리스트. (삭제할 이미지가 없으면 생략 가능)",
            example = "[15, 18]"
    )
    private List<Long> deleteImageIds;
}
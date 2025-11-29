package com.foodcom.firstpro.domain.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import java.time.LocalDateTime;

@Schema(description = "댓글 조회 응답 DTO")
@Getter
public class CommentResponseDto {

    @Schema(description = "댓글 고유 ID (PK)", example = "15")
    private final Long id;

    @Schema(description = "댓글 내용", example = "맛있겠다~~")
    private final String content;

    @Schema(description = "작성자 이름 (닉네임)", example = "홍길동")
    private final String writer;

    @Schema(description = "댓글 작성 시간", example = "2024-11-29T18:30:00")
    private final LocalDateTime createdAt;

    public CommentResponseDto(Comment comment) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.writer = comment.getMember().getLoginId();
        this.createdAt = comment.getCreatedAt();
    }
}
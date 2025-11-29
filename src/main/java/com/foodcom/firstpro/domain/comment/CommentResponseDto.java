package com.foodcom.firstpro.domain.comment;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class CommentResponseDto {
    private final Long id;
    private final String content;
    private final String writer;
    private final LocalDateTime createdAt;

    public CommentResponseDto(Comment comment) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.writer = comment.getMember().getLoginId();
        this.createdAt = comment.getCreatedAt();
    }
}
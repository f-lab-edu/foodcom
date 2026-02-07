package com.foodcom.firstpro.domain.post;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MyPostListResponseDto {

    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private int commentCount;

    public MyPostListResponseDto(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.createdAt = post.getCreatedAt();
        this.commentCount = post.getComments().size();
    }
}

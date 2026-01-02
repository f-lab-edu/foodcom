package com.foodcom.firstpro.domain.post;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Schema(description = "게시물 목록 요약 정보 DTO (홈 화면용)")
public class PostListResponseDto {

    @Schema(description = "게시물 고유 UUID", example = "550e8400-e29b-41d4-a716-446655440000")
    private final String uuid;

    @Schema(description = "게시물 제목", example = "오늘 점심 메뉴 추천합니다!")
    private final String title;

    @Schema(description = "작성자 사용자명(닉네임)", example = "food_lover_123")
    private final String writer;

    @Schema(description = "대표 이미지(썸네일) URL. (이미지가 없으면 null 반환)", example = "https://storage.googleapis.com/my-bucket/post-images/sample.jpg")
    private final String thumbnailUrl;

    @Schema(description = "작성 일시", example = "2024-03-10T14:30:00")
    private final LocalDateTime createdAt;

    @Schema(description = "최근 수정 일시 (수정 안 했으면 작성일과 동일)", example = "2024-03-11T09:15:00")
    private final LocalDateTime modifiedAt;

    @Schema(description = "댓글 개수", example = "12")
    private final int commentCount;

    public PostListResponseDto(Post post) {
        this.uuid = post.getUuid();
        this.title = post.getTitle();
        this.writer = post.getMember().getUsername();
        this.createdAt = post.getCreatedAt();
        this.modifiedAt = post.getModifiedAt();
        this.commentCount = post.getComments().size();

        this.thumbnailUrl = post.getImages().isEmpty()
                ? null
                : post.getImages().getFirst().getUrl();
    }
}
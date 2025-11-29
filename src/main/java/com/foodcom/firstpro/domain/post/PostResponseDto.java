package com.foodcom.firstpro.domain.post;

import com.foodcom.firstpro.domain.comment.CommentResponseDto;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
public class PostResponseDto {

    private String uuid;
    private String title;
    private String content;
    private String userName;
    private LocalDateTime createdAt;

    private List<String> imageUrls;
    private List<CommentResponseDto> comments;

    public PostResponseDto(Post post) {
        this.uuid = post.getUuid();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.userName = post.getMember().getUsername();
        this.createdAt = post.getCreatedAt();

        this.imageUrls = post.getImages().stream()
                .map(Image::getUrl)
                .collect(Collectors.toList());

        this.comments = post.getComments().stream()
                .map(CommentResponseDto::new)
                .collect(Collectors.toList());
    }
}

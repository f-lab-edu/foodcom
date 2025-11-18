package com.foodcom.firstpro.domain.post;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostDto {

    private Long id;
    private String title;
    private LocalDateTime createdAt;

}

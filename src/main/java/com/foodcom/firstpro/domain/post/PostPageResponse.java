package com.foodcom.firstpro.domain.post;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class PostPageResponse {
    private List<PostListResponseDto> postList;
    private long totalElements; // 전체 게시물 수
    private int totalPages;     // 전체 페이지 수
    private int size;           // 한 페이지 크기
    private int number;         // 현재 페이지
    private boolean last;       // 마지막 페이지 여부
    private boolean first;      // 첫 페이지 여부
}
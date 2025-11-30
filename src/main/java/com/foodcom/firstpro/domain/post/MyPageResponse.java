package com.foodcom.firstpro.domain.post;

import com.foodcom.firstpro.domain.member.Gender;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MyPageResponse {

    private final String loginId;
    private final String username;
    private final Gender gender;
    private final Integer age;

    private List<PostListResponseDto> posts;

    private long totalElements; // 전체 게시물 수
    private int totalPages;     // 전체 페이지 수
    private boolean last;       // 마지막 페이지인지 (Next 버튼 비활성화용)
    private int size;           // 한 페이지당 크기
    private int number;         // 현재 페이지 번호

}
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
    private List<PostResponseDto> posts;

}

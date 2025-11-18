package com.foodcom.firstpro.service;

import com.foodcom.firstpro.domain.member.Member;
import com.foodcom.firstpro.domain.post.MyPageResponse;
import com.foodcom.firstpro.domain.post.Post;
import com.foodcom.firstpro.domain.post.PostDto;
import com.foodcom.firstpro.repository.LoginRepository;
import com.foodcom.firstpro.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final LoginRepository memberRepository;
    private final PostRepository postRepository;

    @Transactional
    public MyPageResponse getMyInfo(String userId) {

        Member member = memberRepository.findByLoginId(userId).orElseThrow(
                () -> new UsernameNotFoundException("사용자 id를 찾을 수 없습니다: " + userId)
        );
        List<Post> postList = postRepository.findByMember(member);
        List<PostDto> postDtoList = postList.stream()
                .map(Post::createPostDto)
                .toList();

        return MyPageResponse.builder()
                .loginId(member.getLoginId())
                .posts(postDtoList)
                .username(member.getUsername())
                .age(member.getAge())
                .gender(member.getGender())
                .build();
    }
}

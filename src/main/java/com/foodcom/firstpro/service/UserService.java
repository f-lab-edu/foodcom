package com.foodcom.firstpro.service;

import com.foodcom.firstpro.domain.member.Member;
import com.foodcom.firstpro.domain.member.MemberUpdateDto;
import com.foodcom.firstpro.domain.post.MyPageResponse;
import com.foodcom.firstpro.domain.post.Post;
import com.foodcom.firstpro.domain.post.PostListResponseDto;
import com.foodcom.firstpro.repository.MemberRepository;
import com.foodcom.firstpro.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final BCryptPasswordEncoder encoder;

    @Transactional(readOnly = true)
    public MyPageResponse getMyPageDetails(String loginId, Pageable pageable) {

        Member member = memberRepository.findByLoginId(loginId).orElseThrow(
                () -> new UsernameNotFoundException("사용자 id를 찾을 수 없습니다: " + loginId)
        );

        Page<Post> postPage = postRepository.findByMember(member, pageable);
        List<PostListResponseDto> postListResponseDtoList = postPage.stream()
                .map(PostListResponseDto::new)
                .toList();

        return MyPageResponse.builder()
                .loginId(member.getLoginId())
                .username(member.getUsername())
                .age(member.getAge())
                .gender(member.getGender())

                // 게시물 리스트
                .posts(postListResponseDtoList)

                // 메타데이터
                .totalElements(postPage.getTotalElements())
                .totalPages(postPage.getTotalPages())
                .last(postPage.isLast())
                .size(postPage.getSize())
                .number(postPage.getNumber() + 1)
                .build();
    }

    //만약 회원정보 수정 후 회원정보 반환값이 필요할 경우 DTO 반환
    @Transactional
    public void updateMyInfo(String userId, MemberUpdateDto memberUpdateDto) {

        Member member = memberRepository.findByLoginId(userId).orElseThrow(
                () -> new UsernameNotFoundException("인증된 사용자 ID를 찾을 수 없습니다: " + userId)
        );

        if (memberUpdateDto.getNewPassword() != null && !memberUpdateDto.getNewPassword().isEmpty()) {

            String newPassword = memberUpdateDto.getNewPassword();

            if (!StringUtils.hasText(newPassword)) {
                throw new IllegalArgumentException("새 비밀번호는 공백이 될 수 없습니다.");
            }
            if (encoder.matches(newPassword, member.getPassword())) {
                log.info("새 비밀번호가 기존 비밀번호와 동일합니다.");
            } else {
                String encodedPassword = encoder.encode(newPassword);
                member.updatePassword(encodedPassword);
                log.info("비밀번호를 성공적으로 업데이트했습니다.");
            }
        }
        member.update(memberUpdateDto);
    }

}

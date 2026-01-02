package com.foodcom.firstpro.service;

import com.foodcom.firstpro.auth.exception.LoginFailureException;
import com.foodcom.firstpro.auth.exception.ResourceNotFoundException;
import com.foodcom.firstpro.domain.comment.Comment;
import com.foodcom.firstpro.domain.comment.CommentCreateDto;
import com.foodcom.firstpro.domain.member.Member;
import com.foodcom.firstpro.domain.post.Post;
import com.foodcom.firstpro.repository.CommentRepository;
import com.foodcom.firstpro.repository.MemberRepository;
import com.foodcom.firstpro.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;

    public void createComment(String postUuid, CommentCreateDto commentCreateDto, String loginId) {
        Post post = postRepository.findByUuid(postUuid)
                .orElseThrow(() -> new ResourceNotFoundException("게시물을 찾을 수 없습니다."));

        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new LoginFailureException("회원 정보를 찾을 수 없습니다."));

        Comment comment = Comment.builder()
                .content(commentCreateDto.getContent())
                .post(post)
                .member(member)
                .build();

        commentRepository.save(comment);
    }
}

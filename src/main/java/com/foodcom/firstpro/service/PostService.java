package com.foodcom.firstpro.service;

import com.foodcom.firstpro.auth.exception.ResourceNotFoundException;
import com.foodcom.firstpro.domain.member.Member;
import com.foodcom.firstpro.domain.post.*;
import com.foodcom.firstpro.repository.MemberRepository;
import com.foodcom.firstpro.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import org.springframework.security.access.AccessDeniedException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final StorageService storageService;

    @Transactional
    public Post createPost(String title, String content, List<MultipartFile> imageFiles) throws IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            log.warn(">> [Service] 인증 실패: SecurityContext에 인증 정보 없음.");
            throw new SecurityException("인증되지 않은 사용자입니다.");
        }

        String loginId = authentication.getName(); // testuser123

        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. (Username: " + loginId + ")"));

        Post post = Post.builder()
                .title(title)
                .content(content)
                .member(member)
                .build();

        postRepository.save(post);

        if (imageFiles != null) {
            for (MultipartFile file : imageFiles) {
                if (file != null && !file.isEmpty()) {

                    String pathPrefix = "post-images/" + post.getId();

                    String imageUrl = storageService.uploadFile(file, pathPrefix);
                    String filename = file.getOriginalFilename();

                    Image image = Image.builder()
                            .url(imageUrl)
                            .filename(filename)
                            .post(post)
                            .build();

                    post.addImage(image);
                }
            }
        }

        return post;
    }

    @Transactional(readOnly = true)
    public PostResponseDto getPostInfo(String postUuid) {
        Post post = postRepository.findByUuid(postUuid)
                .orElseThrow(() -> new ResourceNotFoundException("게시물을 찾을 수 없습니다. Uuid = " + postUuid));

        return new PostResponseDto(post);
    }

    @Transactional
    public void updatePost(String postUuid, PostUpdateRequestDto updateDto, List<MultipartFile> newFiles,
            String username) throws IOException {

        Post post = postRepository.findByUuid(postUuid)
                .orElseThrow(() -> new ResourceNotFoundException("게시물을 찾을 수 없습니다."));

        if (!post.getMember().getLoginId().equals(username)) {
            throw new AccessDeniedException("작성자만 게시물을 수정할 수 있습니다.");
        }

        post.updateText(updateDto.getTitle(), updateDto.getContent());

        List<Long> deleteIds = updateDto.getDeleteImageIds();
        if (deleteIds != null && !deleteIds.isEmpty()) {

            Set<Long> deleteIdSet = new HashSet<>(updateDto.getDeleteImageIds());

            List<Image> imagesToDelete = post.getImages().stream()
                    .filter(img -> deleteIdSet.contains(img.getId()))
                    .toList();

            for (Image img : imagesToDelete) {
                storageService.deleteFile(img.getUrl());
            }
            // application.properties에 spring.jpa.properties.hibernate.jdbc.batch_size=50 적용
            post.getImages().removeAll(imagesToDelete);
        }

        if (newFiles != null && !newFiles.isEmpty()) {
            for (MultipartFile file : newFiles) {
                if (file.isEmpty()) {
                    continue;
                }

                String pathPrefix = "post-images/" + post.getId();

                String imageUrl = storageService.uploadFile(file, pathPrefix);

                Image image = Image.builder()
                        .url(imageUrl)
                        .filename(file.getOriginalFilename())
                        .post(post)
                        .build();

                post.addImage(image);
            }
        }
    }

    @Transactional
    public void deletePost(String postUuid, String username) {

        Post post = postRepository.findByUuid(postUuid)
                .orElseThrow(() -> new ResourceNotFoundException("게시물을 찾을 수 없습니다."));

        if (!post.getMember().getLoginId().equals(username)) {
            throw new AccessDeniedException("작성자만 게시물을 삭제할 수 있습니다.");
        }

        List<Image> images = post.getImages();
        if (images != null && !images.isEmpty()) {
            for (Image image : images) {
                storageService.deleteFile(image.getUrl());
            }
        }

        postRepository.delete(post);
    }

    @Transactional(readOnly = true)
    public PostPageResponse getPostList(Pageable pageable) {
        Page<PostListResponseDto> postPage = postRepository.findPostList(pageable);

        return PostPageResponse.builder()
                .postList(postPage.getContent())
                .totalElements(postPage.getTotalElements())
                .totalPages(postPage.getTotalPages())
                .size(postPage.getSize())
                .number(postPage.getNumber() + 1)
                .last(postPage.isLast())
                .first(postPage.isFirst())
                .build();
    }
}
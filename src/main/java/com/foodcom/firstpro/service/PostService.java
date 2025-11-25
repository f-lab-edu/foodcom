package com.foodcom.firstpro.service;

import com.foodcom.firstpro.domain.member.Member;
import com.foodcom.firstpro.domain.post.Image;
import com.foodcom.firstpro.domain.post.Post;
import com.foodcom.firstpro.repository.MemberRepository;
import com.foodcom.firstpro.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final StorageService storageService;

    @Transactional // 데이터를 변경하는 작업이므로 쓰기 트랜잭션 필요
    public Post createPost(String title, String content, List<MultipartFile> imageFiles) throws IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            log.warn(">> [Service] 인증 실패: SecurityContext에 인증 정보 없음.");
            throw new SecurityException("인증되지 않은 사용자입니다.");
        }

        String loginId = authentication.getName(); // testuser123

        log.info(">> [Service] 1단계 성공: SecurityContext에서 사용자 이름({}) 추출 완료.", loginId);

        log.info(">> [Service] 2단계 시작: 사용자 이름({})으로 DB에서 사용자 조회.", loginId);

        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. (Username: " + loginId + ")"));
        log.info(">> [Service] 2단계 성공: 사용자({}) 조회 완료.", member.getLoginId()); // 조회된 loginId 로그


        Post post = Post.builder()
                .title(title)
                .content(content)
                .member(member)
                .build();

        postRepository.save(post);
        log.info(">> [Service] 3단계 성공: Post({}) DB 저장 완료.", post.getId());


        if (imageFiles != null) {
            for (MultipartFile file : imageFiles) {
                if (file != null && !file.isEmpty()) {

                    String pathPrefix = "post-images/" + post.getId();

                    log.info(">> [Service] 4단계 시작: GCS 파일 업로드 시작. 원본 파일명: {}", file.getOriginalFilename());

                    String imageUrl = storageService.uploadFile(file, pathPrefix);
                    String filename = file.getOriginalFilename();

                    log.info(">> [Service] 5단계 성공: GCS 업로드 완료. URL: {}", imageUrl);

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
}
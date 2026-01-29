package com.foodcom.firstpro.repository;

import com.foodcom.firstpro.domain.member.Member;
import com.foodcom.firstpro.domain.post.Post;
import com.foodcom.firstpro.domain.post.PostListResponseDto;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByMember(Member member, Pageable pageable);

    Optional<Post> findByUuid(String uuid);

    @Query("""
    SELECT new com.foodcom.firstpro.domain.post.PostListResponseDto(
        p.uuid,
        p.title,
        m.username,
        p.thumbnailUrl,
        p.createdAt,
        p.modifiedAt,
        p.commentCount
    )
    FROM Post p
    JOIN p.member m
""")
    Page<PostListResponseDto> findPostList(Pageable pageable);

    @Modifying
    @Query("update Post p set p.commentCount = p.commentCount + 1 where p.id = :postId")
    void increaseCommentCount(@Param("postId") Long postId);
}

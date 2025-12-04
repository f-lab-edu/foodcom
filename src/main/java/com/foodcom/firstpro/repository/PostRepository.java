package com.foodcom.firstpro.repository;

import com.foodcom.firstpro.domain.member.Member;
import com.foodcom.firstpro.domain.post.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByMember(Member member, Pageable pageable);

    Optional<Post> findByUuid(String uuid);

    @Query(value = "SELECT p FROM Post p JOIN FETCH p.member",
            countQuery = "SELECT count(p) FROM Post p")
    Page<Post> findAllWithMemberAndImages(Pageable pageable);
}

package com.foodcom.firstpro.repository;

import com.foodcom.firstpro.domain.member.Member;
import com.foodcom.firstpro.domain.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByMember(Member member);
}

package com.foodcom.firstpro.repository;

import com.foodcom.firstpro.domain.comment.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

}

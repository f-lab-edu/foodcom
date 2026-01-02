package com.foodcom.firstpro.domain.member;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMember is a Querydsl query type for Member
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMember extends EntityPathBase<Member> {

    private static final long serialVersionUID = -126699203L;

    public static final QMember member = new QMember("member1");

    public final NumberPath<Integer> age = createNumber("age", Integer.class);

    public final ListPath<com.foodcom.firstpro.domain.comment.Comment, com.foodcom.firstpro.domain.comment.QComment> comments = this.<com.foodcom.firstpro.domain.comment.Comment, com.foodcom.firstpro.domain.comment.QComment>createList("comments", com.foodcom.firstpro.domain.comment.Comment.class, com.foodcom.firstpro.domain.comment.QComment.class, PathInits.DIRECT2);

    public final EnumPath<Gender> gender = createEnum("gender", Gender.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath loginId = createString("loginId");

    public final StringPath password = createString("password");

    public final ListPath<com.foodcom.firstpro.domain.post.Post, com.foodcom.firstpro.domain.post.QPost> posts = this.<com.foodcom.firstpro.domain.post.Post, com.foodcom.firstpro.domain.post.QPost>createList("posts", com.foodcom.firstpro.domain.post.Post.class, com.foodcom.firstpro.domain.post.QPost.class, PathInits.DIRECT2);

    public final StringPath username = createString("username");

    public final StringPath uuid = createString("uuid");

    public QMember(String variable) {
        super(Member.class, forVariable(variable));
    }

    public QMember(Path<? extends Member> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMember(PathMetadata metadata) {
        super(Member.class, metadata);
    }

}


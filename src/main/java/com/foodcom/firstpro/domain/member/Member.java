package com.foodcom.firstpro.domain.member;

import com.foodcom.firstpro.domain.comment.Comment;
import com.foodcom.firstpro.domain.post.Post;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "member", indexes = {
        @Index(name = "idx_login_id", columnList = "loginId")
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String loginId;

    @Column(nullable = false, length = 100)
    private String password;

    @NotNull
    private String username;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Gender gender;

    @NotNull
    private Integer age;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    public void update(MemberUpdateDto memberUpdateDto) {

        if (memberUpdateDto.getNewName() != null &&
                !this.getUsername().equals(memberUpdateDto.getNewName())) {
            this.username = memberUpdateDto.getNewName();
        }

        // 2. 성별 수정
        if (memberUpdateDto.getGender() != null &&
                !this.getGender().equals(memberUpdateDto.getGender())) {
            this.gender = memberUpdateDto.getGender();
        }

        // 3. 나이 수정
        if (memberUpdateDto.getAge() != null &&
                !this.getAge().equals(memberUpdateDto.getAge())) {
            this.age = memberUpdateDto.getAge();
        }
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public static Member createMember(MemberJoinDTO memberJoinDTO) {
        return Member.builder()
                .loginId(memberJoinDTO.getLoginId())
                .password(memberJoinDTO.getPassword())
                .username(memberJoinDTO.getUsername())
                .gender(memberJoinDTO.getGender())
                .age(memberJoinDTO.getAge())
                .build();
    }
}

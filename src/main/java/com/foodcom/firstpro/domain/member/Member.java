package com.foodcom.firstpro.domain.member;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Table(name = "member")
@Data
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String loginId;

    @NotNull
    private String password;

    @NotNull
    private String username;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Gender gender;

    @NotNull
    private Integer age;

    public Member() {
    }

    public static Member createMember(MemberJoinDTO memberJoinDTO) {
        Member member = new Member();
        member.setLoginId(memberJoinDTO.getLoginId());
        member.setPassword(memberJoinDTO.getPassword());
        member.setUsername(memberJoinDTO.getUsername());
        member.setGender(memberJoinDTO.getGender());
        member.setAge(memberJoinDTO.getAge());
        return member;
    }
}

package com.foodcom.firstpro.domain.member;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "member")
@Data
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String loginId;

    private String password;

    private String username;

    @Enumerated(EnumType.STRING)
    private Gender gender;

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

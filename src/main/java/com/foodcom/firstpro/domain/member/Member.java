package com.foodcom.firstpro.domain.member;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "member")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String uuid;

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

    @PrePersist
    public void generateUuid() {
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID().toString();
        }
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

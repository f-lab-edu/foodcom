package com.foodcom.firstpro.repository;


import com.foodcom.firstpro.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoginRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByLoginId(String loginId);

    // 회원가입 시 중복 ID를 확인하는 메서드
    boolean existsByLoginId(String loginId);

    // 비밀번호 중보도 확인
    boolean existsByPassword(String password);
}

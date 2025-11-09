package com.foodcom.firstpro.repository;


import com.foodcom.firstpro.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoginRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByLoginId(String loginId);

}

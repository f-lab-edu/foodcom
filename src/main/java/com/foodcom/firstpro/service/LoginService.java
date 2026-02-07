package com.foodcom.firstpro.service;

import com.foodcom.firstpro.domain.member.Member;
import com.foodcom.firstpro.domain.member.MemberJoinDTO;
import com.foodcom.firstpro.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder encoder;

    @Transactional
    public String join(MemberJoinDTO memberJoinDTO) {

        if (memberRepository.findByLoginId(memberJoinDTO.getLoginId()).isPresent()) {
            throw new IllegalStateException("이미 사용 중인 아이디입니다.");
        }
        log.info("CI/CD test");
        // 단방향 해시 함수를 이용하여 비밀번호 암호화
        memberJoinDTO.setPassword(encoder.encode(memberJoinDTO.getPassword()));

        Member member = Member.createMember(memberJoinDTO);
        memberRepository.save(member);

        return member.getId().toString();
    }

}

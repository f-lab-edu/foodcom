package com.foodcom.firstpro.service;

import com.foodcom.firstpro.domain.member.Member;
import com.foodcom.firstpro.domain.member.MemberJoinDTO;
import com.foodcom.firstpro.repository.LoginRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {

    private final LoginRepository loginRepository;
    private final BCryptPasswordEncoder encoder;

    @Transactional
    public String join(MemberJoinDTO memberJoinDTO) {

        //단방향 해시 함수를 이용하여 비밀번호 암호화
        memberJoinDTO.setPassword(encoder.encode(memberJoinDTO.getPassword()));

        Member member = Member.createMember(memberJoinDTO);
        loginRepository.save(member);

        return member.getUuid();
    }

}

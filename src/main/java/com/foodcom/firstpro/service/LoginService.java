package com.foodcom.firstpro.service;

import com.foodcom.firstpro.domain.member.Member;
import com.foodcom.firstpro.domain.member.MemberJoinDTO;
import com.foodcom.firstpro.domain.member.MemberLoginDTO;
import com.foodcom.firstpro.repository.LoginRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {

    private final LoginRepository loginRepository;
    private final BCryptPasswordEncoder encoder;

    @Transactional
    public Long join(MemberJoinDTO memberJoinDTO) {


        //단방향 해시 함수를 이용하여 비밀번호 암호화
        memberJoinDTO.setPassword(encoder.encode(memberJoinDTO.getPassword()));

        log.info("암호화 성공");
        //중복 제거
        validateDuplicateMember(memberJoinDTO);

        Member member = Member.createMember(memberJoinDTO);
        loginRepository.save(member);

        //로그인 이후에 Id 이용해서 활용해야 하니까 Id 반환
        return member.getId();
    }

    @Transactional
    public Long login(MemberLoginDTO memberLoginDTO) {

        Member member = loginRepository.findByLoginId(memberLoginDTO.getLoginId())
                .orElseThrow(()->new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (!encoder.matches(memberLoginDTO.getPassword(),member.getPassword())) {
            throw new IllegalArgumentException("비밀번호를 다시 입력해주세요.");
        }
        return member.getId();
    }

    // 비밀번호 또는 아이디 중복일때 회원가입시 예외 생성해주는 메서드
    private void validateDuplicateMember(MemberJoinDTO memberJoinDTO) {
        if (loginRepository.existsByLoginId(memberJoinDTO.getLoginId())) {
            throw new IllegalStateException("이미 존재하는 회원 아이디입니다.");
        }
        if (loginRepository.existsByPassword(memberJoinDTO.getPassword())) {
            throw new IllegalStateException("이미 존재하는 비밀번호입니다.");
        }
    }
}

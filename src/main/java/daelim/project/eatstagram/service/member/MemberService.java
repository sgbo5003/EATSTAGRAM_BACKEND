package daelim.project.eatstagram.service.member;

import daelim.project.eatstagram.service.member.MemberRole;
import daelim.project.eatstagram.security.dto.ValidationMemberDTO;
import daelim.project.eatstagram.service.base.BaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService extends BaseService<String, Member, MemberDTO, MemberRepository> {

    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public ValidationMemberDTO join(ValidationMemberDTO dto) {
        Optional<Member> result =  getRepository().findByUsernameAndFormSocial(dto.getUsername(), false);
        if (result.isPresent()) {
            dto.setMsg(dto.getUsername() + "은 이미 존재하는 아이디입니다.");
            return dto;
        } else {
            Member member = Member.builder()
                        .username(dto.getUsername())
                        .password(passwordEncoder.encode(dto.getPassword()))
                        .email(dto.getEmail())
                        .name(dto.getName())
                        .nickname(dto.getNickname())
                        .formSocial(false)
                        .build();
            member.addMemberRole(MemberRole.USER);
            getRepository().save(member);
        }
        dto.setMsg("회원가입 성공");
        dto.setJoinSuccessYn(true);
        return dto;
    }

    public String checkUsername(String username) {
        Optional<Member> result = getRepository().findByUsernameAndFormSocial(username,false);
        if (result.isPresent()) {
            return "fail";
        }
        return "ok";
    }

    public String checkNickname(String nickname) {
        Optional<Member> result =  getRepository().findMemberByNickname(nickname);
        if (result.isPresent()) {
            return "fail";
        } else {
            return "ok";
        }
    }
}

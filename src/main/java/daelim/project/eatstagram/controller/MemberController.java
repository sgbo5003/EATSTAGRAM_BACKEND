package daelim.project.eatstagram.controller;

import daelim.project.eatstagram.security.dto.AuthMemberDTO;
import daelim.project.eatstagram.security.dto.ValidationMemberDTO;
import daelim.project.eatstagram.service.emailAuth.EmailAuthDTO;
import daelim.project.eatstagram.service.emailAuth.EmailAuthService;
import daelim.project.eatstagram.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final MemberService memberService;
    private final EmailAuthService emailAuthService;
    private ValidationMemberDTO validationMemberDTO;

    // 로그인 성공
    @RequestMapping("loginSuccess")
    @ResponseBody
    public ResponseEntity<String> loginSuccess(@AuthenticationPrincipal AuthMemberDTO authMemberDTO) {
        return new ResponseEntity<>("{\"response\": \"ok\", \"username\": \"" + authMemberDTO.getUsername() + "\"}", HttpStatus.OK);
    }

    // 로그인 실패
    @RequestMapping("loginFail")
    @ResponseBody
    public ResponseEntity<String> loginFail(HttpServletRequest request) {
        return new ResponseEntity<>("{\"response\": \"fail\", \"msg\": \"" + request.getAttribute("msg") + "\"}", HttpStatus.OK);
    }

    // 사용자 아이디 중복확인
    @RequestMapping("/checkUsername")
    @ResponseBody
    public ResponseEntity<String> checkUsername(String username) {
        return new ResponseEntity<>("{\"response\": \"" + memberService.checkUsername(username) + "\"}" , HttpStatus.OK);
    }

    // 사용자 닉네임 중복확인
    @RequestMapping("/checkNickname")
    @ResponseBody
    public ResponseEntity<String> checkNickname(String nickname) {
        return new ResponseEntity<>("{\"response\": " + "\"" + memberService.checkNickname(nickname) + "\"}" , HttpStatus.OK);
    }

    // 회원가입 1단계 : 회원 정보 유효성 검사
    @RequestMapping("/join/step/one")
    @ResponseBody
    public ResponseEntity<Object> joinStepOne(@ModelAttribute @Valid ValidationMemberDTO validationMemberDTO, Errors errors) {
        if (errors.hasErrors()) {
            log.info("-----회원가입 유효성 검사 오류 종류-----");
            for (FieldError error : errors.getFieldErrors()) {
                log.info(String.format("valid_%s", error.getField()) + " : " + error.getDefaultMessage());
            }
            log.info("------------------------------------");
            return new ResponseEntity<>("{\"response\": \"error\"}", HttpStatus.OK);
        } else {
            this.validationMemberDTO = validationMemberDTO;
            return new ResponseEntity<>("{\"response\": \"ok\", \"joinToken\": \"true\"}", HttpStatus.OK);
        }
    }

    // 회원가입 2단계 : 이메일 인증번호 발송
    @RequestMapping("/join/step/two")
    @ResponseBody
    public ResponseEntity<EmailAuthDTO> joinStepTwo() {
        EmailAuthDTO emailAuthenticationDTO =
                emailAuthService.createEmailCertificationNumber(
                        validationMemberDTO.getUsername(),
                        validationMemberDTO.getEmail());
        return new ResponseEntity<>(emailAuthenticationDTO, HttpStatus.OK);
    }

    // 회원가입 3단계 : 회원가입 완료
    @RequestMapping("/join/step/three")
    @ResponseBody
    public ResponseEntity<ValidationMemberDTO> joinStepThree(String emailAuthId) {
        emailAuthService.confirmEmail(emailAuthId);
        return new ResponseEntity<>(memberService.join(validationMemberDTO), HttpStatus.OK);
    }

    @RequestMapping("/getUser")
    @ResponseBody
    public ResponseEntity<AuthMemberDTO> getUser(@AuthenticationPrincipal AuthMemberDTO authMemberDTO) {
        return new ResponseEntity<>(authMemberDTO, HttpStatus.OK);
    }
}

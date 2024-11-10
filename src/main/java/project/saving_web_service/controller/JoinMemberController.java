package project.saving_web_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import project.saving_web_service.domain.Member;
import project.saving_web_service.service.MemberService;

@Controller
@RequiredArgsConstructor
public class JoinMemberController {
    private final MemberService memberService;

    // 중복 체크 여부와 확인된 아이디를 저장하는 변수
    private boolean duplicateChecked = false;
    private String checkedLoginId = "";

    // 회원가입 페이지 폼 표시
    @GetMapping("/members/new")
    public String createForm(Model model) {
        model.addAttribute("memberForm", new MemberForm());

        // 주의: 페이지를 다시 로드할 때 중복 체크 상태를 초기화하는 부분이 문제일 수 있음
        duplicateChecked = false;
        checkedLoginId = "";
        return "members/createMemberForm";
    }

    // 회원가입 처리
    @PostMapping("/members/new")
    public String create(@Valid MemberForm form, BindingResult result) {
        // 중복 확인이 제대로 이루어졌는지 검증
        if (!duplicateChecked || !form.getLogin_id().equals(checkedLoginId)) {
            // 중복 확인이 안 된 경우, 에러 메시지 추가
            result.reject("duplicateCheck", "아이디 중복 확인을 해주세요.");
            return "members/createMemberForm"; // 수정 필요: 중복 확인 후 페이지를 다시 로드할 때 데이터가 사라질 수 있음
        }

        // 유효성 검사 실패 시 폼 다시 로드
        if (result.hasErrors()) {
            return "members/createMemberForm";
        }

        // 새로운 회원 생성
        Member member = new Member();

        // 회원 정보 설정 (여기에서 NullPointerException 발생 가능성 있음)
        member.setLogin_id(form.getLogin_id());
        member.setPassword(form.getPassword());

        // 주의: form.getStatus()가 null일 경우 오류 발생 가능
        if (form.getStatus() != null) {
            member.setStatus(String.join(",", form.getStatus()));
        }

        member.setAge(form.getAge());
        member.setSex(form.getSex());
        member.setPurpose(form.getPurpose());
        member.setPreferredCondition(form.getPreferredCondition());
        member.setPeriod(form.getPeriod());

        // 주의: form.getImportant()가 null일 경우 오류 발생 가능
        if (form.getImportant() != null) {
            member.setImportant(String.join(",", form.getImportant()));
        }

        member.setAmount(form.getAmount());

        // 회원 정보 저장
        memberService.join(member); // 이 부분에서 데이터베이스에 문제가 있을 경우 예외 발생 가능

        return "redirect:/";
    }

    // 중복 확인 API
    @GetMapping("/api/members/check-duplicate")
    @ResponseBody
    public ResponseEntity<String> checkDuplicate(@RequestParam("login_id") String login_id) {
        // 중복된 아이디 체크
        if (memberService.isDuplicateLoginId(login_id)) {
            return ResponseEntity.ok("duplicate");
        } else {
            duplicateChecked = true;
            checkedLoginId = login_id;
            return ResponseEntity.ok("available");
        }
    }
}

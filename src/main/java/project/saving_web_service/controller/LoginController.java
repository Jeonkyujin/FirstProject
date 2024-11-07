package project.saving_web_service.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import project.saving_web_service.domain.Member;
import project.saving_web_service.service.MemberService;
import project.saving_web_service.service.NewsService;  // 뉴스 서비스 추가
import project.saving_web_service.service.RedisService;
import project.saving_web_service.service.RestApiService;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private final MemberService memberService;
    private final NewsService newsService;
    private final RestApiService restApiService;
    private final RedisService redisService;

    @GetMapping("/login")
    public String login(HttpSession httpSession, Model model) throws JsonProcessingException {
        String login_id = (String) httpSession.getAttribute("login_id");
        String notebookResult = restApiService.execute(login_id);
        Member member = memberService.findMember(login_id);
        String a = "abc";

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> notebookData = objectMapper.readValue(notebookResult, Map.class);

        // install_data와 deposit_data를 추출
        List<Map<String, Object>> installDataList = (List<Map<String, Object>>) notebookData.get("install_data");
        List<Map<String, Object>> depositDataList = (List<Map<String, Object>>) notebookData.get("deposit_data");

        Set<String> strings = redisService.viewedData(member.getAge(), member.getSex(), 5);



        model.addAttribute("installData", installDataList);
        model.addAttribute("depositData", installDataList);
        model.addAttribute("string", strings);
        model.addAttribute("member", member);
        model.addAttribute("login_id", login_id);
        List<Map<String, String>> newsList = newsService.getLatestNews();
        model.addAttribute("newsList", newsList);
        if (httpSession.getAttribute("firstLogin") == null) {
            httpSession.setAttribute("firstLogin", true);
            httpSession.setAttribute("currentTopRanking", a);
            model.addAttribute("notebookResult", notebookResult); // notebookResult에 알림 데이터를 설정
        } else {
            model.addAttribute("notebookResult", null); // 이미 로그인한 상태라면 null로 설정
        }
        return "login/login.html";
    }

}

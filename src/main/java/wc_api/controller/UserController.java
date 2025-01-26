package wc_api.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import wc_api.common.constant.ApiRespPolicy;
import wc_api.common.model.response.ApiResp;
import wc_api.model.request.UserLoginReq;
import wc_api.model.request.UserReq;
import wc_api.model.response.UserResp;
import wc_api.service.UserService;

/**
 * 유저 컨트롤러
 *
 * @author 김창민
 */

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    /**
     * 회원가입
     *
     * @param userReq 유저정보 객체
     * @return 회원가입 성공여부
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResp> signup(@RequestBody @Valid UserReq userReq) throws Exception {
        userService.createUser(userReq);
        return ResponseEntity.status(ApiRespPolicy.SUCCESS_CREATED.getHttpStatus()).body(ApiResp.of(ApiRespPolicy.SUCCESS_CREATED));
    }

    /**
     * 로그인
     *
     * @param userReq 유저정보 객체
     * @return 로그인 성공 여부와 accessToken, refreshToken이 포함된 웅답 객체 반환
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResp> login(@RequestBody @Valid UserLoginReq userReq) throws Exception {
        UserResp result = userService.login(userReq);
        return ResponseEntity.status(ApiRespPolicy.SUCCESS.getHttpStatus()).body(ApiResp.of(ApiRespPolicy.SUCCESS, result));
    }

    /**
     * 토큰 갱신
     *
     * @param request HTTP 요청 객체, refreshToken을 헤더에서 추출
     * @return 새로 발급된 accessToken, refreshToken이 포함된 응답 객체 반환
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResp> refresh(HttpServletRequest request) throws Exception {
        String refreshToken = request.getHeader("refreshToken");
        UserResp result = userService.refresh(refreshToken);
        return ResponseEntity.status(ApiRespPolicy.SUCCESS_ISSUE_TOKEN.getHttpStatus()).body(ApiResp.of(ApiRespPolicy.SUCCESS_ISSUE_TOKEN, result));
    }
}

package wc_api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import wc_api.common.constant.ApiRespPolicy;
import wc_api.common.model.response.ApiResp;
import wc_api.service.UserService;

/**
 * 유저 컨트롤러
 *
 * @author 김창민
 */

@RestController
@RequestMapping("/api/login")
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
    public ResponseEntity<ApiResp> signup(@RequestBody @Valid ApiResp userReq) throws Exception {
        userService.createUser(userReq);
        return ResponseEntity.status(ApiRespPolicy.SUCCESS_CREATED.getHttpStatus()).body(ApiResp.of(ApiRespPolicy.SUCCESS_CREATED));
    }
}

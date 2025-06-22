package wc_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import wc_api.common.constant.ApiRespPolicy;
import wc_api.common.model.response.ApiResp;
import wc_api.model.db.user.User;
import wc_api.model.request.UserLoginReq;
import wc_api.model.request.UserReq;
import wc_api.model.response.UserResp;
import wc_api.service.UserService;

import java.io.IOException;

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
     * Authorization 헤더에서 JWT 토큰을 추출하고 사용자 ID를 반환
     */
    private Integer extractUserIdFromToken(HttpServletRequest request) throws Exception {
        String accessToken = request.getHeader("Authorization");
        if (accessToken == null || !accessToken.startsWith("Bearer ")) {
            throw new IllegalArgumentException("유효하지 않은 Authorization 헤더입니다.");
        }
        accessToken = accessToken.substring(7);
        return userService.getUserIdFromToken(accessToken);
    }

    // =============== 기존 인증 관련 엔드포인트들 (변경 없음) ===============

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
     * @return 로그인 성공 여부와 accessToken, refreshToken이 포함된 응답 객체 반환
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

    /**
     * 로그아웃
     *
     * @param request HTTP 요청 객체, accessToken을 헤더에서 추출
     * @return 로그아웃 성공 여부
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResp> logout(HttpServletRequest request) throws Exception {
        String accessToken = request.getHeader("Authorization");
        if (accessToken != null && accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7);
        }
        userService.logout(accessToken);
        return ResponseEntity.status(ApiRespPolicy.SUCCESS.getHttpStatus()).body(ApiResp.of(ApiRespPolicy.SUCCESS));
    }

    // =============== 🔥 새로 추가된 프로필 이미지 CRUD 엔드포인트들 ===============

    /**
     * 프로필 이미지와 함께 회원가입
     *
     * @param userStr 유저정보 JSON 문자열
     * @param profileImage 프로필 이미지 파일 (선택사항)
     * @return 회원가입 성공여부
     */
    @PostMapping(
            value = "/signup-with-image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ApiResp> signupWithProfileImage(
            @RequestPart(value = "user") String userStr,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) throws IOException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            UserReq userReq = mapper.readValue(userStr, UserReq.class);

            userService.createUserWithProfileImage(userReq, profileImage);
            return ResponseEntity.status(ApiRespPolicy.SUCCESS_CREATED.getHttpStatus())
                    .body(ApiResp.of(ApiRespPolicy.SUCCESS_CREATED));
        } catch (Exception e) {
            System.out.println("프로필 이미지와 함께 회원가입 에러: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(ApiRespPolicy.ERR_SYSTEM.getHttpStatus())
                    .body(ApiResp.of(ApiRespPolicy.ERR_SYSTEM, e.getMessage()));
        }
    }

    /**
     * 프로필 이미지 업로드/수정
     *
     * @param profileImage 프로필 이미지 파일
     * @param request HTTP 요청 객체 (JWT 토큰에서 사용자 ID 추출)
     * @return 업데이트된 사용자 정보
     */
    @PostMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResp> uploadProfileImage(
            @RequestPart("profileImage") MultipartFile profileImage,
            HttpServletRequest request) throws IOException {
        try {
            Integer userId = extractUserIdFromToken(request);
            User updatedUser = userService.updateProfileImage(userId, profileImage);
            return ResponseEntity.status(ApiRespPolicy.SUCCESS.getHttpStatus())
                    .body(ApiResp.of(ApiRespPolicy.SUCCESS, updatedUser));
        } catch (Exception e) {
            System.out.println("프로필 이미지 업로드 에러: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(ApiRespPolicy.ERR_NOT_AUTHENTICATED.getHttpStatus())
                    .body(ApiResp.of(ApiRespPolicy.ERR_NOT_AUTHENTICATED, e.getMessage()));
        }
    }

    /**
     * 프로필 이미지 수정 (PUT 방식)
     *
     * @param profileImage 새로운 프로필 이미지 파일
     * @param request HTTP 요청 객체 (JWT 토큰에서 사용자 ID 추출)
     * @return 업데이트된 사용자 정보
     */
    @PutMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResp> updateProfileImage(
            @RequestPart("profileImage") MultipartFile profileImage,
            HttpServletRequest request) throws IOException {
        try {
            Integer userId = extractUserIdFromToken(request);
            User updatedUser = userService.updateProfileImage(userId, profileImage);
            return ResponseEntity.status(ApiRespPolicy.SUCCESS.getHttpStatus())
                    .body(ApiResp.of(ApiRespPolicy.SUCCESS, updatedUser));
        } catch (Exception e) {
            System.out.println("프로필 이미지 수정 에러: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(ApiRespPolicy.ERR_NOT_AUTHENTICATED.getHttpStatus())
                    .body(ApiResp.of(ApiRespPolicy.ERR_NOT_AUTHENTICATED, e.getMessage()));
        }
    }

    /**
     * 내 프로필 정보 조회 (프로필 이미지 포함)
     *
     * @param request HTTP 요청 객체 (JWT 토큰에서 사용자 ID 추출)
     * @return 사용자 프로필 정보
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResp> getMyProfile(HttpServletRequest request) {
        try {
            Integer userId = extractUserIdFromToken(request);
            User userProfile = userService.getUserProfile(userId);
            return ResponseEntity.status(ApiRespPolicy.SUCCESS.getHttpStatus())
                    .body(ApiResp.of(ApiRespPolicy.SUCCESS, userProfile));
        } catch (Exception e) {
            System.out.println("프로필 조회 에러: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(ApiRespPolicy.ERR_NOT_AUTHENTICATED.getHttpStatus())
                    .body(ApiResp.of(ApiRespPolicy.ERR_NOT_AUTHENTICATED, e.getMessage()));
        }
    }

    /**
     * 프로필 이미지 삭제
     *
     * @param request HTTP 요청 객체 (JWT 토큰에서 사용자 ID 추출)
     * @return 업데이트된 사용자 정보
     */
    @DeleteMapping("/profile-image")
    public ResponseEntity<ApiResp> deleteProfileImage(HttpServletRequest request) {
        try {
            Integer userId = extractUserIdFromToken(request);
            User updatedUser = userService.deleteProfileImage(userId);
            return ResponseEntity.status(ApiRespPolicy.SUCCESS.getHttpStatus())
                    .body(ApiResp.of(ApiRespPolicy.SUCCESS, updatedUser));
        } catch (Exception e) {
            System.out.println("프로필 이미지 삭제 에러: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(ApiRespPolicy.ERR_NOT_AUTHENTICATED.getHttpStatus())
                    .body(ApiResp.of(ApiRespPolicy.ERR_NOT_AUTHENTICATED, e.getMessage()));
        }
    }

    /**
     * 프로필 정보 수정 (이미지 + 기본 정보)
     *
     * @param userStr 수정할 사용자 정보 JSON 문자열
     * @param profileImage 새로운 프로필 이미지 (선택사항)
     * @param request HTTP 요청 객체 (JWT 토큰에서 사용자 ID 추출)
     * @return 업데이트된 사용자 정보
     */
    @PutMapping(
            value = "/profile",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ApiResp> updateProfile(
            @RequestPart(value = "user") String userStr,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            HttpServletRequest request) throws IOException {
        try {
            Integer userId = extractUserIdFromToken(request);
            ObjectMapper mapper = new ObjectMapper();
            UserReq userReq = mapper.readValue(userStr, UserReq.class);

            User updatedUser = userService.updateUserProfile(userId, userReq, profileImage);
            return ResponseEntity.status(ApiRespPolicy.SUCCESS.getHttpStatus())
                    .body(ApiResp.of(ApiRespPolicy.SUCCESS, updatedUser));
        } catch (Exception e) {
            System.out.println("프로필 수정 에러: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(ApiRespPolicy.ERR_NOT_AUTHENTICATED.getHttpStatus())
                    .body(ApiResp.of(ApiRespPolicy.ERR_NOT_AUTHENTICATED, e.getMessage()));
        }
    }
}
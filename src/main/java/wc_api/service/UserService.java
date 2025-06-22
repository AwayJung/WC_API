package wc_api.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import wc_api.common.constant.ApiRespPolicy;
import wc_api.common.exception.CommonApiException;
import wc_api.common.jwt.JwtUtil;
import wc_api.dao.UserDAO;
import wc_api.model.db.user.User;
import wc_api.model.request.UserLoginReq;
import wc_api.model.request.UserReq;
import wc_api.model.response.UserResp;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserDAO userDAO;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ModelMapper modelMapper;

    // ItemController에서 사용하던 것과 동일한 ImageService를 재사용
    @Autowired
    private ImageService imageService;

    /**
     * 회원가입
     *
     * @param userReq 유저정보 객체
     * @throws CommonApiException with {@link ApiRespPolicy#ERR_DUPLICATED_USER} 중복된 유저가 존재할 때
     */
    @Transactional
    public void createUser(UserReq userReq) throws Exception {
        try {
            User existUser = userDAO.getUserByEmail(userReq.getLoginEmail());
            if (existUser != null) {
                throw new CommonApiException(ApiRespPolicy.ERR_DUPLICATED_USER);
            }
            String encodedPassword = passwordEncoder.encode(userReq.getPassword());
            User user = new User();
            user.setName(userReq.getName());
            user.setLoginEmail(userReq.getLoginEmail());
            user.setPassword(encodedPassword);

            userDAO.createUser(user);
        } catch (Exception e) {
            // TODO: Logging 처리
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 로그인
     *
     * @param userReq 유저정보 객체
     * @throws CommonApiException with {@link ApiRespPolicy#ERR_NOT_AUTHENTICATED} 인증되지 않은 유저일 때
     */
    @Transactional
    public UserResp login(UserLoginReq userReq) throws Exception {
        try {
            String loginEmail = userReq.getLoginEmail(); // 현재 로그인 되어있는 사용자의 이메일
            User userFromDB = userDAO.getUserByEmail(loginEmail); // DB에서 현재 로그인 되어있는 사용자 정보를 가져옴

            if(userFromDB == null || !passwordEncoder.matches(userReq.getPassword(), userFromDB.getPassword())) {
                throw new CommonApiException(ApiRespPolicy.ERR_NOT_AUTHENTICATED);
            }
            // access token, refresh token 발급
            String accessToken = jwtUtil.createAccessToken(userFromDB.getLoginEmail());
            String refreshToken = jwtUtil.createRefreshToken(userFromDB.getLoginEmail());

            // user객체에 refresh token 저장
            userFromDB.setRefreshToken(refreshToken);

            // DB에 저장
            userDAO.updateRefreshToken(userFromDB.getLoginEmail(), userFromDB.getRefreshToken());

            UserResp userResp = modelMapper.map(userFromDB, UserResp.class);
            System.out.println("userFromDB.getRegDt() : "+ userFromDB.getRegDt());
            userResp.setRegDt(userFromDB.getRegDt());
            userResp.setUserId(userFromDB.getId());
            userResp.setAccessToken(accessToken);
            userResp.setRefreshToken(refreshToken);

            return userResp;

        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 토큰 갱신
     *
     * @param refreshToken 리프레쉬토큰
     * @throws CommonApiException with {@link ApiRespPolicy#ERR_INVALID_REFRESH_TOKEN} 리프레쉬토큰이 유효하지 않을 때
     */
    @Transactional
    public UserResp refresh(String refreshToken) throws Exception {
        try {
            String loginEmail = jwtUtil.extractLoginEmail(refreshToken);

            String storedRefreshToken = userDAO.getStoredRefreshToken(loginEmail);

            if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
                throw new CommonApiException(ApiRespPolicy.ERR_INVALID_REFRESH_TOKEN);
            }

            String newAccessToken = jwtUtil.createAccessToken(loginEmail);
            String newRefreshToken = jwtUtil.createRefreshToken(loginEmail);

            userDAO.storeRefreshToken(loginEmail, newRefreshToken);

            UserResp userResp = new UserResp();
            userResp.setAccessToken(newAccessToken);
            userResp.setRefreshToken(newRefreshToken);

            return userResp;

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 로그아웃
     *
     * @param accessToken 액세스토큰
     * @throws CommonApiException with {@link ApiRespPolicy#ERR_NOT_AUTHENTICATED} 인증되지 않은 유저일 때
     */
    @Transactional
    public void logout(String accessToken) throws Exception {
        try {
            if (accessToken == null || accessToken.isEmpty()) {
                throw new CommonApiException(ApiRespPolicy.ERR_NOT_AUTHENTICATED);
            }

            // 토큰에서 이메일 추출
            String loginEmail = jwtUtil.extractLoginEmail(accessToken);
            if (loginEmail == null) {
                throw new CommonApiException(ApiRespPolicy.ERR_NOT_AUTHENTICATED);
            }

            // Refresh Token 삭제 (DB에서 null로 설정)
            userDAO.updateRefreshToken(loginEmail, null);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * JWT 토큰에서 사용자 ID를 추출
     *
     * @param accessToken JWT 액세스 토큰
     * @return 사용자 ID
     * @throws CommonApiException 토큰이 유효하지 않거나 사용자를 찾을 수 없을 때
     */
    public Integer getUserIdFromToken(String accessToken) throws Exception {
        try {
            // 토큰에서 이메일 추출
            String loginEmail = jwtUtil.extractLoginEmail(accessToken);
            if (loginEmail == null) {
                throw new CommonApiException(ApiRespPolicy.ERR_NOT_AUTHENTICATED);
            }

            // 이메일로 사용자 정보 조회
            User user = userDAO.getUserByEmail(loginEmail);
            if (user == null) {
                throw new CommonApiException(ApiRespPolicy.ERR_NOT_AUTHENTICATED);
            }

            return user.getId();  // int 타입의 ID 반환
        } catch (Exception e) {
            e.printStackTrace();
            throw new CommonApiException(ApiRespPolicy.ERR_NOT_AUTHENTICATED);
        }
    }

    // =============== 🔥 새로 추가된 프로필 이미지 관련 메서드들 ===============

    /**
     * 프로필 이미지 업데이트
     * - 기존 이미지가 있다면 삭제 후 새 이미지 저장
     * - 새 이미지 파일명을 DB에 업데이트
     *
     * @param userId 사용자 ID (JWT 토큰에서 추출됨)
     * @param profileImage 업로드할 프로필 이미지 파일
     * @return 업데이트된 사용자 정보
     * @throws Exception 파일 저장 실패, 사용자 없음 등의 경우
     */
    @Transactional
    public User updateProfileImage(Integer userId, MultipartFile profileImage) throws Exception {
        try {
            // 1. 사용자 존재 여부 확인
            User user = userDAO.getUserById(userId);
            if (user == null) {
                throw new CommonApiException(ApiRespPolicy.ERR_NOT_AUTHENTICATED);
            }

            // 2. 기존 프로필 이미지가 있다면 하드디스크에서 삭제
            if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
                // ImageService의 deleteImage 메서드를 사용해 파일 삭제
                imageService.deleteImage(user.getProfileImage());
            }

            // 3. 새로운 이미지가 있다면 저장
            String savedFileName = null;
            if (profileImage != null && !profileImage.isEmpty()) {
                // ImageService의 storeImage 메서드를 사용해 파일 저장
                // ItemController에서 사용하던 것과 동일한 방식
                savedFileName = imageService.storeImage(profileImage);
            }

            // 4. DB에 새로운 파일명 업데이트
            user.setProfileImage(savedFileName);
            userDAO.updateUserProfileImage(userId, savedFileName);

            return user;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 사용자 프로필 정보 조회
     * - 프로필 이미지 파일명 포함한 전체 사용자 정보 반환
     *
     * @param userId 사용자 ID
     * @return 사용자 프로필 정보 (프로필 이미지 파일명 포함)
     * @throws Exception 사용자가 존재하지 않을 때
     */
    public User getUserProfile(Integer userId) throws Exception {
        try {
            User user = userDAO.getUserById(userId);
            if (user == null) {
                throw new CommonApiException(ApiRespPolicy.ERR_NOT_AUTHENTICATED);
            }
            return user;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 프로필 이미지 삭제
     * - 하드디스크에서 이미지 파일 삭제
     * - DB에서 프로필 이미지 파일명을 null로 설정
     *
     * @param userId 사용자 ID
     * @return 업데이트된 사용자 정보
     * @throws Exception 파일 삭제 실패, 사용자 없음 등의 경우
     */
    @Transactional
    public User deleteProfileImage(Integer userId) throws Exception {
        try {
            // 1. 사용자 존재 여부 확인
            User user = userDAO.getUserById(userId);
            if (user == null) {
                throw new CommonApiException(ApiRespPolicy.ERR_NOT_AUTHENTICATED);
            }

            // 2. 기존 이미지 파일이 있다면 하드디스크에서 삭제
            if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
                imageService.deleteImage(user.getProfileImage());
            }

            // 3. DB에서 프로필 이미지 파일명을 null로 업데이트
            user.setProfileImage(null);
            userDAO.updateUserProfileImage(userId, null);

            return user;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 프로필 이미지와 함께 회원가입
     * - 회원가입과 동시에 프로필 이미지 업로드
     *
     * @param userReq 유저정보 객체
     * @param profileImage 프로필 이미지 파일 (선택사항)
     * @throws Exception 회원가입 실패, 이미지 저장 실패 등의 경우
     */
    @Transactional
    public void createUserWithProfileImage(UserReq userReq, MultipartFile profileImage) throws Exception {
        try {
            // 1. 중복 사용자 체크 (기존 로직과 동일)
            User existUser = userDAO.getUserByEmail(userReq.getLoginEmail());
            if (existUser != null) {
                throw new CommonApiException(ApiRespPolicy.ERR_DUPLICATED_USER);
            }

            // 2. 프로필 이미지 저장 (있는 경우만)
            String savedFileName = null;
            if (profileImage != null && !profileImage.isEmpty()) {
                savedFileName = imageService.storeImage(profileImage);
            }

            // 3. 사용자 정보 생성 (프로필 이미지 파일명 포함)
            String encodedPassword = passwordEncoder.encode(userReq.getPassword());
            User user = new User();
            user.setName(userReq.getName());
            user.setLoginEmail(userReq.getLoginEmail());
            user.setPassword(encodedPassword);
            user.setProfileImage(savedFileName); // 프로필 이미지 파일명 설정
            user.setIntroduction(userReq.getIntroduction());

            // 4. DB에 저장
            userDAO.createUser(user);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 프로필 정보 수정 (프로필 이미지 포함)
     * - 사용자 기본 정보와 프로필 이미지를 함께 수정
     *
     * @param userId 사용자 ID
     * @param userReq 수정할 사용자 정보
     * @param profileImage 새로운 프로필 이미지 (선택사항)
     * @return 업데이트된 사용자 정보
     * @throws Exception 수정 실패, 이미지 저장 실패 등의 경우
     */
    @Transactional
    public User updateUserProfile(Integer userId, UserReq userReq, MultipartFile profileImage) throws Exception {
        try {
            // 1. 사용자 존재 여부 확인
            User user = userDAO.getUserById(userId);
            if (user == null) {
                throw new CommonApiException(ApiRespPolicy.ERR_NOT_AUTHENTICATED);
            }

            // 2. 프로필 이미지 처리 (새로운 이미지가 있는 경우만)
            if (profileImage != null && !profileImage.isEmpty()) {
                // 기존 이미지 삭제
                if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
                    imageService.deleteImage(user.getProfileImage());
                }
                // 새 이미지 저장
                String savedFileName = imageService.storeImage(profileImage);
                user.setProfileImage(savedFileName);
            }

            // 3. 사용자 기본 정보 업데이트
            user.setName(userReq.getName());
            // 필요한 다른 필드들도 여기서 업데이트...

            // 4. DB 업데이트 (이 메서드는 UserDAO에 추가해야 함)
            userDAO.updateUserProfile(user);

            return user;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }
    /**
     * 자기소개만 업데이트
     * - 자기소개만 따로 수정하고 싶을 때 사용
     *
     * @param userId 사용자 ID
     * @param introduction 새로운 자기소개 (null 가능)
     * @return 업데이트된 사용자 정보
     * @throws Exception 사용자 없음 등의 경우
     */
    @Transactional
    public User updateIntroduction(Integer userId, String introduction) throws Exception {
        try {
            // 1. 사용자 존재 여부 확인
            User user = userDAO.getUserById(userId);
            if (user == null) {
                throw new CommonApiException(ApiRespPolicy.ERR_NOT_AUTHENTICATED);
            }

            // 2. 자기소개 업데이트
            user.setIntroduction(introduction);
            userDAO.updateUserIntroduction(userId, introduction);

            return user;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
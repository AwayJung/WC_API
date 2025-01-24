package wc_api.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wc_api.common.constant.ApiRespPolicy;
import wc_api.common.exception.CommonApiException;
import wc_api.common.jwt.JwtUtil;
import wc_api.common.model.response.ApiResp;
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
            int updateResult = userDAO.updateRefreshToken(userFromDB.getLoginEmail(), userFromDB.getRefreshToken());

            UserResp userResp = modelMapper.map(userFromDB, UserResp.class);
            userResp.setAccessToken(accessToken);
            userResp.setRefreshToken(refreshToken);

            return userResp;


        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}

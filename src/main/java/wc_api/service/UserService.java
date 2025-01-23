package wc_api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wc_api.common.constant.ApiRespPolicy;
import wc_api.common.exception.CommonApiException;
import wc_api.common.model.response.ApiResp;
import wc_api.dao.UserDAO;
import wc_api.model.db.user.User;
import wc_api.model.request.UserReq;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserDAO userDAO;

    private final BCryptPasswordEncoder passwordEncoder;
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


}

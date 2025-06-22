package wc_api.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import wc_api.model.db.user.User;

@Mapper
public interface UserDAO {

    User getUserByEmail(@Param(value = "loginEmail") String loginEmail);

    int createUser(@Param("user") User user) throws Exception;

    int updateRefreshToken(@Param("loginEmail") String loginEmail, @Param("refreshToken") String refreshToken);

    String getStoredRefreshToken(@Param("loginEmail") String loginEmail);

    void storeRefreshToken(@Param("loginEmail") String loginEmail, @Param("newRefreshToken") String newRefreshToken);

    /**
     * 사용자 ID로 사용자 정보 조회
     * - 프로필 조회, 이미지 업데이트 시 사용
     *
     * @param userId 사용자 ID
     * @return 사용자 정보 (프로필 이미지 파일명 포함)
     */
    User getUserById(@Param("userId") Integer userId);

    /**
     * 프로필 이미지 파일명만 업데이트
     * - 이미지 업로드/수정/삭제 시 사용
     *
     * @param userId 사용자 ID
     * @param profileImage 프로필 이미지 파일명 (삭제 시 null)
     * @return 업데이트된 행 수
     */
    int updateUserProfileImage(@Param("userId") Integer userId,
                               @Param("profileImage") String profileImage);

    /**
     * 사용자 프로필 전체 정보 업데이트
     * - 이름, 프로필 이미지, 자기소개 등 전체 프로필 수정 시 사용
     *
     * @param user 업데이트할 사용자 정보
     * @return 업데이트된 행 수
     */
    int updateUserProfile(@Param("user") User user);

    /**
     * 자기소개만 업데이트
     * - 자기소개만 따로 수정하고 싶을 때 사용
     *
     * @param userId 사용자 ID
     * @param introduction 새로운 자기소개 (null 가능)
     * @return 업데이트된 행 수
     */
    int updateUserIntroduction(@Param("userId") Integer userId,
                               @Param("introduction") String introduction);

    /**
     * 사용자 비밀번호 업데이트
     * - 임시 비밀번호 설정, 비밀번호 변경 시 사용
     *
     * @param userId 사용자 ID
     * @param password 암호화된 비밀번호
     * @return 업데이트된 행 수
     */
    int updateUserPassword(@Param("userId") Integer userId,
                           @Param("password") String password);
}
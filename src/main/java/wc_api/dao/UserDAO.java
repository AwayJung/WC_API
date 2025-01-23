package wc_api.dao;

import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;
import wc_api.model.db.user.User;

@Repository
public interface UserDAO {

    User getUserByEmail(@Param(value = "loginEmail") String loginEmail);

    int createUser(@Param("user") User user) throws Exception;
}

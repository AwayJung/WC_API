package wc_api.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import wc_api.model.db.user.User;

@Mapper
public interface UserDAO {

    User getUserByEmail(@Param(value = "loginEmail") String loginEmail);

    int createUser(@Param("user") User user) throws Exception;
}

package wc_api.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserReq {
    // 유저 로그인 이메일
    @NotBlank
    @Email(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
    private String loginEmail;

    // 유저 로그인 비밀번호
    @NotBlank
    @Size(min = 10, max = 30)
    private String password;

    // 유저 이름
    @NotBlank
    @Size(min = 2, max = 20)
    private String name;

    private String introduction;
    private String nickname;
}

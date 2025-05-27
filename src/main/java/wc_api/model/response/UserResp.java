package wc_api.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * 유저 API 응답 모델
 *
 * @author 김창민
 */
@Data
public class UserResp {
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private int userId;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String loginEmail;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String name;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String accessToken;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String refreshToken;
}

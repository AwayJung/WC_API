package wc_api.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

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
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")  // JSON 직렬화 형식 지정
    private LocalDateTime regDt;
}

package wc_api.common.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import wc_api.common.constant.ApiRespPolicy;
import wc_api.common.util.MessageUtil;

/**
 * API 요청 처리 예외
 *
 * @author 김창민
 */

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class CommonApiException extends Exception {

    private ApiRespPolicy apiRespPolicy;

    public CommonApiException(String message) {
        super(message);
    }

    public CommonApiException(ApiRespPolicy apiRespPolicy) {
        this(MessageUtil.getMessage(apiRespPolicy.getMessageKey()), apiRespPolicy);
    }

    public CommonApiException(String message, ApiRespPolicy apiRespPolicy) {
        super(message);
        this.apiRespPolicy = apiRespPolicy;
    }

}
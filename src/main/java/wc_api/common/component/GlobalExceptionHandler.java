package wc_api.common.component;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import wc_api.common.constant.ApiRespPolicy;
import wc_api.common.exception.CommonApiException;
import wc_api.common.model.response.ApiResp;

/**
 * 예외 처리 핸들러
 *
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 특정되지 않은 모든 예외를 처리한다.
     *
     * @param e 예외 객체
     * @return HTTP 에러 응답
     * @see ApiRespPolicy
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResp> handleAllException(final Exception e) {
        return ResponseEntity.status(ApiRespPolicy.ERR_SYSTEM.getHttpStatus()).body(ApiResp.withError(ApiRespPolicy.ERR_SYSTEM));
    }

    /**
     * API 요청 처리 시 발생하는 예외를 처리한다.
     *
     * @param e {@link CommonApiException} API 요청 처리 예외
     * @return HTTP 에러 응답
     * @see ApiRespPolicy
     */
    @ExceptionHandler(CommonApiException.class)
    public ResponseEntity<ApiResp> handleCommonApiException(final CommonApiException e) {
        ApiRespPolicy apiRespPolicy = e.getApiRespPolicy();
        return ResponseEntity.status(apiRespPolicy.getHttpStatus()).body(ApiResp.withError(apiRespPolicy));
    }

    /**
     * 매개변수 검증 예외를 처리한다.
     *
     * @param e {@link MethodArgumentNotValidException} 매개변수 검증 예외
     * @return HTTP 에러 응답
     * @see ApiRespPolicy#ERR_INVALID_PARAMS
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResp> handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        return ResponseEntity.status(ApiRespPolicy.ERR_INVALID_PARAMS.getHttpStatus())
                .body(ApiResp.withFieldErrors(ApiRespPolicy.ERR_INVALID_PARAMS, e.getBindingResult()));
    }

}


package wc_api.model.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatMessageResponse {
    private int messageId;
    private String content;
    private LocalDateTime sentTime;
    private int senderId;
    private String senderName; // user 테이블 조인
}
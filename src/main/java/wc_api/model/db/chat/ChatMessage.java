package wc_api.model.db.chat;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatMessage {
    private Integer messageId;
    private String content;
    private String type;
    private LocalDateTime sentTime;
    private boolean isRead;
    private String roomId;
    private Integer senderId;  // userId -> senderId로 변경
    private String userType;

    public Integer getUserId() {  // 호환성을 위해 유지
        return senderId;
    }

    public void setUserId(Integer userId) {
        this.senderId = userId;
    }
}
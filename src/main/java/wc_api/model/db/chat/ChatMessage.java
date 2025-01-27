package wc_api.model.db.chat;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatMessage {
    private Long messageId;          // 메시지 ID
    private String roomId;           // 채팅방 ID
    private int sender;           // 보내는 사람
    private int receiver;         // 받는 사람
    private String message;          // 메시지 내용
    private LocalDateTime sentTime;  // 전송 시간
    private boolean isRead;          // 읽음 여부
}


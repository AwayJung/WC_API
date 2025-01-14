package wc_api.model.db.chat;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatMessage {
    private MessageType type;         // 메시지 타입 (START, TALK, END)
    private String roomId;            // 채팅방 ID
    private String sender;            // 보내는 사람
    private String message;           // 메시지 내용
    private LocalDateTime time;       // 메시지 전송 시간
    private String receiver;          // 받는 사람
    private boolean isRead;           // 읽음 여부
    private Long itemId;              // 거래 상품 ID
    private Integer price;            // 가격 제안
    private Status status;            // 메시지 상태

    // 메시지 타입 enum: 열거형
    public enum MessageType {
        START,    // 대화 시작
        TALK,     // 대화
        END       // 대화 종료
    }

    // 메시지 상태 enum
    public enum Status {
        UNREAD,     // 읽지 않음
        READ         // 읽음
    }
}


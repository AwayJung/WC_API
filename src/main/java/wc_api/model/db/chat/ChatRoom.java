package wc_api.model.db.chat;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatRoom {
    private String roomId;        // 채팅방 ID
    private Long itemId;          // 상품 ID
    private int sellerId;      // 판매자 ID
    private int buyerId;       // 구매자 ID
    private LocalDateTime createdAt;  // 생성 시간
}
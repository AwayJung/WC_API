package wc_api.model.db.chat;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatRoom {
    private String roomId;
    private int itemId;
    private LocalDateTime createdAt;
    private String description;
    private int price;

    private String lastMessage;
    private LocalDateTime lastMessageTime;

    private String name;              // 채팅방 이름 (상대방 이름 + 역할)
    private String itemTitle;         // 상품 제목
    private String sellerNickname;    // 판매자 닉네임
}
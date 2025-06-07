package wc_api.model.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatRoomResponse {
    private String roomId;
    private int itemId;
    private String itemImage;  // item 테이블 조인
    private int sellerName;     // item 테이블 조인
    private String lastMessage; // message 테이블 조인
    private LocalDateTime lastMessageTime;

}

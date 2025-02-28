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
}
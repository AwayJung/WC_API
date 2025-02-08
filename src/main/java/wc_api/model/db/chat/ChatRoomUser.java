package wc_api.model.db.chat;

import lombok.Data;

@Data
public class ChatRoomUser {
    private String roomId;
    private int userId;
    private String userType;
    private int messageId;
}

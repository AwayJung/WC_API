package wc_api.model.db.chat;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatMessage {
    private int messageId;
    private String content;
    private String type;
    private LocalDateTime sentTime;
    private boolean isRead;
    private String roomId;
    private int senderId;
}

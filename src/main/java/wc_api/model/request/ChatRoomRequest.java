package wc_api.model.request;

import lombok.Data;

@Data
public class ChatRoomRequest {
    private Long itemId;
    private String sellerId;
    private String buyerId;
}
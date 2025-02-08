package wc_api.model.request;

import lombok.Data;

@Data
public class ChatRoomCreateRequest {
    private int itemId;
    private int buyerId;
}
package wc_api.model.request;

import lombok.Data;

@Data
public class MessageRequest {
    private String roomId;
    private String userId;
}
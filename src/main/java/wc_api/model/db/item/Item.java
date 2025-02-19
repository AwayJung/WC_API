package wc_api.model.db.item;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Item {
    private Long itemId;
    private Long sellerId;
    private String description;
    private int price;
    private LocalDateTime createdAt;

    // 추가 필드 - JOIN 결과를 받기 위한 필드
    private String sellerEmail;
    private String sellerName;
}

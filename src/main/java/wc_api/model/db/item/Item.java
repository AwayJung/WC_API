package wc_api.model.db.item;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Item {
    private Long itemId;
    private Long sellerId;
    private String description;
    private int price;
    private LocalDateTime createdAt;
    private String imageUrl;
    private String title;
    private String additionalImages; // 추가된 필드: 추가 이미지 URL들을 JSON 형태로 저장
    private int viewCount; // 조회수 필드 추가
    private Boolean isLiked;  // 현재 사용자의 찜 상태
    private Long likeCount;  // 해당 아이템의 전체 찜 횟수
    private int categoryId;
    private String categoryName;
    private Boolean priceFlexible;
    private String status; // 상품 상태(판매중, 판매완료)

    // 추가 필드 - JOIN 결과를 받기 위한 필드
    private String sellerEmail;
    private String sellerName;

    // 조회 결과용 필드 - 추가 이미지 URL 리스트
    private List<String> imageUrlList;

    // 수정 요청 시 유지할 이미지 ID 목록
    private List<String> imageIds;
}
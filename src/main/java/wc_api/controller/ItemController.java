package wc_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import wc_api.common.constant.ApiRespPolicy;
import wc_api.common.model.response.ApiResp;
import wc_api.model.db.item.Item;
import wc_api.service.ItemLikeService;
import wc_api.service.ItemService;
import wc_api.service.UserService; // JWT 토큰 파싱을 위해 추가

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:3000"})
public class ItemController {

    private final ItemService itemService;
    private final ItemLikeService itemLikeService;
    private final UserService userService; // JWT 토큰 파싱을 위해 추가

    /**
     * Authorization 헤더에서 JWT 토큰을 추출하고 사용자 ID를 반환
     */
    private Integer extractUserIdFromToken(HttpServletRequest request) throws Exception {
        String accessToken = request.getHeader("Authorization");
        if (accessToken == null || !accessToken.startsWith("Bearer ")) {
            throw new IllegalArgumentException("유효하지 않은 Authorization 헤더입니다.");
        }
        accessToken = accessToken.substring(7);
        return userService.getUserIdFromToken(accessToken);
    }

    @PostMapping("/{itemId}/like")
    public ResponseEntity<ApiResp> toggleItemLike(
            @PathVariable Long itemId,
            HttpServletRequest request
    ) {
        try {
            Integer userId = extractUserIdFromToken(request);
            boolean isLiked = itemLikeService.toggleItemLike(userId, itemId);
            return ResponseEntity
                    .status(ApiRespPolicy.SUCCESS.getHttpStatus())
                    .body(ApiResp.of(ApiRespPolicy.SUCCESS, isLiked));
        } catch (Exception e) {
            return ResponseEntity
                    .status(ApiRespPolicy.ERR_NOT_AUTHENTICATED.getHttpStatus())
                    .body(ApiResp.of(ApiRespPolicy.ERR_NOT_AUTHENTICATED, e.getMessage()));
        }
    }

    // 내 찜 목록 조회
    @GetMapping("/my-likes")
    public ResponseEntity<ApiResp> getMyLikedItems(HttpServletRequest request) {
        try {
            Integer userId = extractUserIdFromToken(request);
            List<Item> likedItems = itemLikeService.getMyLikedItems(userId);
            return ResponseEntity
                    .status(ApiRespPolicy.SUCCESS.getHttpStatus())
                    .body(ApiResp.of(ApiRespPolicy.SUCCESS, likedItems));
        } catch (Exception e) {
            return ResponseEntity
                    .status(ApiRespPolicy.ERR_NOT_AUTHENTICATED.getHttpStatus())
                    .body(ApiResp.of(ApiRespPolicy.ERR_NOT_AUTHENTICATED, e.getMessage()));
        }
    }

    // 아이템 상세 조회 시 해당 아이템의 찜 상태도 함께 반환
    @GetMapping("/{itemId}/like-status")
    public ResponseEntity<ApiResp> getItemWithLikeStatus(
            @PathVariable Long itemId,
            HttpServletRequest request
    ) {
        try {
            Integer userId = extractUserIdFromToken(request);
            Item itemDetail = itemLikeService.getItemDetailWithLikeStatus(itemId, userId);
            return ResponseEntity
                    .status(ApiRespPolicy.SUCCESS.getHttpStatus())
                    .body(ApiResp.of(ApiRespPolicy.SUCCESS, itemDetail));
        } catch (Exception e) {
            return ResponseEntity
                    .status(ApiRespPolicy.ERR_NOT_AUTHENTICATED.getHttpStatus())
                    .body(ApiResp.of(ApiRespPolicy.ERR_NOT_AUTHENTICATED, e.getMessage()));
        }
    }

    // 아이템 등록 (다중 이미지 처리)
    @PostMapping(
            value = "/",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ApiResp> createItemWithImages(
            @RequestPart(value = "item") String itemStr,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) throws IOException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Item item = mapper.readValue(itemStr, Item.class);

            Item created = itemService.createItemWithImages(item, images);
            return ResponseEntity
                    .status(ApiRespPolicy.SUCCESS.getHttpStatus())
                    .body(ApiResp.of(ApiRespPolicy.SUCCESS, created));
        } catch (Exception e) {
            System.out.println("에러 발생: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // 단일 이미지 처리 메소드 (기존 코드 호환성)
    @PostMapping(
            value = "/single",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ApiResp> createItemWithImage(
            @RequestPart(value = "item") String itemStr,
            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Item item = mapper.readValue(itemStr, Item.class);

            Item created = itemService.createItemWithImage(item, image);
            return ResponseEntity
                    .status(ApiRespPolicy.SUCCESS.getHttpStatus())
                    .body(ApiResp.of(ApiRespPolicy.SUCCESS, created));
        } catch (Exception e) {
            System.out.println("에러 발생: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // 아이템 이미지만 업데이트
    @PutMapping(value = "/{itemId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResp> updateItemImage(
            @PathVariable Long itemId,
            @RequestPart("image") MultipartFile image) throws IOException {
        Item updated = itemService.updateItemImage(itemId, image);
        return ResponseEntity
                .status(ApiRespPolicy.SUCCESS.getHttpStatus())
                .body(ApiResp.of(ApiRespPolicy.SUCCESS, updated));
    }

    // 아이템 상세 조회
    @GetMapping("/{itemId}")
    public ResponseEntity<ApiResp> getItem(@PathVariable Long itemId) {
        Item item = itemService.getItemAndIncrementViewCount(itemId);
        return ResponseEntity
                .status(ApiRespPolicy.SUCCESS.getHttpStatus())
                .body(ApiResp.of(ApiRespPolicy.SUCCESS, item));
    }

    // 아이템 목록 조회
    @GetMapping("/")
    public ResponseEntity<ApiResp> getItemList() {
        List<Item> items = itemService.getItemList();
        return ResponseEntity
                .status(ApiRespPolicy.SUCCESS.getHttpStatus())
                .body(ApiResp.of(ApiRespPolicy.SUCCESS, items));
    }

    // 판매자(sellerId)별 아이템 목록 조회
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<ApiResp> getSellerItem(@PathVariable Long sellerId) {
        List<Item> items = itemService.getItemsBySellerId(sellerId);
        return ResponseEntity.status(ApiRespPolicy.SUCCESS.getHttpStatus()).body(ApiResp.of(ApiRespPolicy.SUCCESS, items));
    }

    // 조회수 높은 순으로 아이템 목록 조회
    @GetMapping("/popular")
    public ResponseEntity<ApiResp> getPopularItems() {
        List<Item> items = itemService.getItemsOrderByViewCount();
        return ResponseEntity
                .status(ApiRespPolicy.SUCCESS.getHttpStatus())
                .body(ApiResp.of(ApiRespPolicy.SUCCESS, items));
    }

    // 아이템 수정 (다중 이미지 처리)
    @PutMapping(
            value = "/{itemId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ApiResp> updateItem(
            @PathVariable Long itemId,
            @RequestPart(value = "item") String itemStr,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) throws IOException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Item item = mapper.readValue(itemStr, Item.class);
            item.setItemId(itemId);

            Item updated = itemService.updateItemWithImages(item, images);
            return ResponseEntity
                    .status(ApiRespPolicy.SUCCESS.getHttpStatus())
                    .body(ApiResp.of(ApiRespPolicy.SUCCESS, updated));
        } catch (Exception e) {
            System.out.println("에러 발생: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity
                    .status(ApiRespPolicy.ERR_SYSTEM.getHttpStatus())
                    .body(ApiResp.of(ApiRespPolicy.ERR_SYSTEM, e.getMessage()));
        }
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResp> getItemsByCategory(@PathVariable Long categoryId) {
        List<Item> items = itemService.getItemsByCategory(categoryId);
        return ResponseEntity
                .status(ApiRespPolicy.SUCCESS.getHttpStatus())
                .body(ApiResp.of(ApiRespPolicy.SUCCESS, items));
    }

    // 아이템 삭제
    @DeleteMapping("/{itemId}")
    public ResponseEntity<ApiResp> deleteItem(@PathVariable Long itemId) {
        itemService.deleteItem(itemId);
        return ResponseEntity
                .status(ApiRespPolicy.SUCCESS.getHttpStatus())
                .body(ApiResp.of(ApiRespPolicy.SUCCESS, null));
    }

    @PutMapping("/{itemId}/status")
    public ResponseEntity<ApiResp> updateItemStatus(
            @PathVariable Long itemId,
            @RequestBody Map<String, String> statusRequest,
            HttpServletRequest request
    ) {
        try {
            Integer userId = extractUserIdFromToken(request);
            String status = statusRequest.get("status");
            Item updated = itemService.updateItemStatus(itemId, status, userId);
            return ResponseEntity
                    .status(ApiRespPolicy.SUCCESS.getHttpStatus())
                    .body(ApiResp.of(ApiRespPolicy.SUCCESS, updated));
        } catch (Exception e) {
            System.out.println("상태 변경 에러: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity
                    .status(ApiRespPolicy.ERR_SYSTEM.getHttpStatus())
                    .body(ApiResp.of(ApiRespPolicy.ERR_SYSTEM, e.getMessage()));
        }
    }
}
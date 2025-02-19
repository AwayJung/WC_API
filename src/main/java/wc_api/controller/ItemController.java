package wc_api.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import wc_api.common.constant.ApiRespPolicy;
import wc_api.common.model.response.ApiResp;
import wc_api.model.db.item.Item;
import wc_api.service.ItemService;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    // 아이템 등록
    @PostMapping("/")
    public ResponseEntity<ApiResp> createItem(@RequestBody Item item) {
        System.out.println("요청 받은 데이터: " + item);
        System.out.println("sellerId: " + item.getSellerId());
        System.out.println("description: " + item.getDescription());
        System.out.println("price: " + item.getPrice());

        try {
            Item created = itemService.createItem(item);
            System.out.println("생성된 아이템: " + created);
            return ResponseEntity
                    .status(ApiRespPolicy.SUCCESS.getHttpStatus())
                    .body(ApiResp.of(ApiRespPolicy.SUCCESS, created));
        } catch (Exception e) {
            System.out.println("에러 발생: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // 아이템 상세 조회
    @GetMapping("/{itemId}")
    public ResponseEntity<ApiResp> getItem(@PathVariable Long itemId) {
        Item item = itemService.getItem(itemId);
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

    // 아이템 수정
    @PutMapping("/{itemId}")
    public ResponseEntity<ApiResp> updateItem(@PathVariable Long itemId, @RequestBody Item item) {
        item.setItemId(itemId);
        Item updated = itemService.updateItem(item);
        return ResponseEntity
                .status(ApiRespPolicy.SUCCESS.getHttpStatus())
                .body(ApiResp.of(ApiRespPolicy.SUCCESS, updated));
    }

    // 아이템 삭제
    @DeleteMapping("/{itemId}")
    public ResponseEntity<ApiResp> deleteItem(@PathVariable Long itemId) {
        itemService.deleteItem(itemId);
        return ResponseEntity
                .status(ApiRespPolicy.SUCCESS.getHttpStatus())
                .body(ApiResp.of(ApiRespPolicy.SUCCESS, null));
    }
}
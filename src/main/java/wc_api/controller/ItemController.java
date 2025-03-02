package wc_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import wc_api.common.constant.ApiRespPolicy;
import wc_api.common.model.response.ApiResp;
import wc_api.model.db.item.Item;
import wc_api.service.ItemService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:3000"})
public class ItemController {

    private final ItemService itemService;

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
package wc_api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import wc_api.dao.ItemDAO;
import wc_api.model.db.item.Item;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemDAO itemDAO;

    @Value("${app.config.file.path}")
    private String uploadPath;

    // 아이템 생성 (이미지 포함)
    public Item createItemWithImage(Item item, MultipartFile image) throws IOException {
        item.setCreatedAt(LocalDateTime.now());

        if (image != null && !image.isEmpty()) {
            String imageUrl = saveImage(image);
            item.setImageUrl(imageUrl);
        }

        itemDAO.insertItem(item);
        return itemDAO.selectItem(item.getItemId());
    }

    // 이미지 저장
    private String saveImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) return null;

        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = UUID.randomUUID() + fileExtension;

        File saveFile = new File(uploadPath, filename);
        file.transferTo(saveFile);

        // 이미지 URL 경로 추가
        return "/images/" + filename;
    }

    // 아이템 이미지만 업데이트
    public Item updateItemImage(Long itemId, MultipartFile image) throws IOException {
        Item existingItem = itemDAO.selectItem(itemId);
        if (existingItem == null) {
            throw new IllegalArgumentException("Item not found with id: " + itemId);
        }

        // 기존 이미지가 있다면 삭제
        if (existingItem.getImageUrl() != null) {
            String filename = existingItem.getImageUrl().replace("/images/", "");  // /images/ 경로 제거
            new File(uploadPath, filename).delete();  // 올바른 경로 조합
        }

        // 새 이미지 저장
        String newImageUrl = saveImage(image);
        existingItem.setImageUrl(newImageUrl);

        itemDAO.updateItem(existingItem);
        return itemDAO.selectItem(itemId);
    }

    // 아이템 조회
    public Item getItem(Long itemId) {
        Item item = itemDAO.selectItem(itemId);
        if (item == null) {
            throw new IllegalArgumentException("Item not found with id: " + itemId);
        }
        return item;
    }

    // 아이템 목록 조회
    public List<Item> getItemList() {
        return itemDAO.selectItemList();
    }

    // 아이템 정보 업데이트
    public Item updateItem(Item item) {
        Item existingItem = itemDAO.selectItem(item.getItemId());
        if (existingItem == null) {
            throw new IllegalArgumentException("Item not found with id: " + item.getItemId());
        }

        item.setImageUrl(existingItem.getImageUrl());
        item.setCreatedAt(existingItem.getCreatedAt());

        itemDAO.updateItem(item);
        return itemDAO.selectItem(item.getItemId());
    }

    // 아이템 삭제
    public void deleteItem(Long itemId) {
        Item existingItem = itemDAO.selectItem(itemId);
        if (existingItem == null) {
            throw new IllegalArgumentException("Item not found with id: " + itemId);
        }

        // 이미지가 있다면 삭제
        if (existingItem.getImageUrl() != null) {
            String filename = existingItem.getImageUrl().replace("/images/", "");
            new File(uploadPath, filename).delete();
        }

        itemDAO.deleteItem(itemId);
    }
}
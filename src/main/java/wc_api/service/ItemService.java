package wc_api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import wc_api.dao.ItemDAO;
import wc_api.model.db.item.Item;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemDAO itemDAO;

    @Value("${app.config.file.path}")
    private String fileStoragePath;

    // 동적으로 이미지 업로드 경로 생성
    private String getImageUploadPath() {
        return fileStoragePath + "items/";
    }

    // 아이템 생성 (이미지 포함)
    public Item createItemWithImage(Item item, MultipartFile image) throws IOException {
        // 생성 시간 설정
        item.setCreatedAt(LocalDateTime.now());

        // 이미지가 있는 경우 처리
        if (image != null && !image.isEmpty()) {
            String imageUrl = saveImage(image);
            item.setImageUrl(imageUrl);  // imagePath -> imageUrl
        }

        // 아이템 생성
        itemDAO.insertItem(item);

        // 생성된 아이템 반환
        return itemDAO.selectItem(item.getItemId());
    }

    // 아이템 이미지만 업데이트
    public Item updateItemImage(Long itemId, MultipartFile image) throws IOException {
        // 기존 아이템 존재 여부 확인
        Item existingItem = itemDAO.selectItem(itemId);
        if (existingItem == null) {
            throw new IllegalArgumentException("Item not found with id: " + itemId);
        }

        // 기존 이미지가 있다면 삭제
        if (existingItem.getImageUrl() != null) {  // imagePath -> imageUrl
            deleteExistingImage(existingItem.getImageUrl());  // imagePath -> imageUrl
        }

        // 새 이미지 저장
        String newImageUrl = saveImage(image);  // imagePath -> imageUrl
        existingItem.setImageUrl(newImageUrl);  // imagePath -> imageUrl

        // DB 업데이트
        itemDAO.updateItem(existingItem);

        return itemDAO.selectItem(itemId);
    }

    // 이미지 저장 helper 메소드
    private String saveImage(MultipartFile image) throws IOException {
        String uploadPath = getImageUploadPath();
        Path uploadDir = Paths.get(uploadPath);

        // 디버깅을 위한 로그
        System.out.println("Upload path: " + uploadPath);

        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        String originalFilename = image.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFileName = UUID.randomUUID().toString() + fileExtension;

        Path filePath = uploadDir.resolve(newFileName);
        Files.copy(image.getInputStream(), filePath);

        return "items/" + newFileName;  // DB에는 상대 경로만 저장
    }

    // 기존 이미지 삭제 helper 메소드
    private void deleteExistingImage(String imageUrl) {  // imagePath -> imageUrl
        try {
            Path fullPath = Paths.get(fileStoragePath, imageUrl);  // imagePath -> imageUrl
            Files.deleteIfExists(fullPath);
        } catch (IOException e) {
            // 로그 처리
            System.out.println("Failed to delete image: " + imageUrl);  // imagePath -> imageUrl
            e.printStackTrace();
        }
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

        // 기존 이미지 경로와 생성 시간 유지
        item.setImageUrl(existingItem.getImageUrl());  // imagePath -> imageUrl
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
        if (existingItem.getImageUrl() != null) {  // imagePath -> imageUrl
            deleteExistingImage(existingItem.getImageUrl());  // imagePath -> imageUrl
        }

        itemDAO.deleteItem(itemId);
    }
}
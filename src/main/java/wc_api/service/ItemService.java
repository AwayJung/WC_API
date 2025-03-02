package wc_api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import wc_api.dao.ItemDAO;
import wc_api.model.db.item.Item;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemDAO itemDAO;
    private final ImageService imageService;
    private final ObjectMapper objectMapper;

    // 다중 이미지로 아이템 생성
    public Item createItemWithImages(Item item, List<MultipartFile> images) throws IOException {
        item.setCreatedAt(LocalDateTime.now());

        if (images != null && !images.isEmpty()) {
            List<String> imageUrls = new ArrayList<>();

            // 첫 번째 이미지는 대표 이미지로 설정
            String mainImageName = imageService.storeImage(images.get(0));
            item.setImageUrl("/images/" + mainImageName);

            // 나머지 이미지 처리
            for (int i = 1; i < images.size(); i++) {
                if (!images.get(i).isEmpty()) {
                    String imageName = imageService.storeImage(images.get(i));
                    imageUrls.add("/images/" + imageName);
                }
            }

            // 추가 이미지가 있으면 JSON으로 저장
            if (!imageUrls.isEmpty()) {
                item.setAdditionalImages(objectMapper.writeValueAsString(imageUrls));
            }
        }

        itemDAO.insertItem(item);
        return getItemWithImageList(item.getItemId());
    }

    // 기존 단일 이미지 메소드 (하위 호환성)
    public Item createItemWithImage(Item item, MultipartFile image) throws IOException {
        List<MultipartFile> images = new ArrayList<>();
        if (image != null && !image.isEmpty()) {
            images.add(image);
        }
        return createItemWithImages(item, images);
    }

    // 아이템 조회 시 이미지 목록 설정
    private Item getItemWithImageList(Long itemId) {
        Item item = itemDAO.selectItem(itemId);
        if (item == null) {
            throw new IllegalArgumentException("Item not found with id: " + itemId);
        }

        List<String> imageUrls = new ArrayList<>();

        // 추가 이미지가 있으면 파싱
        if (item.getAdditionalImages() != null && !item.getAdditionalImages().isEmpty()) {
            try {
                imageUrls = objectMapper.readValue(
                        item.getAdditionalImages(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
                );
            } catch (Exception e) {
                System.err.println("Error parsing additional images: " + e.getMessage());
            }
        }

        item.setImageUrlList(imageUrls);
        return item;
    }

    // 아이템 조회
    public Item getItem(Long itemId) {
        return getItemWithImageList(itemId);
    }

    // 아이템 목록 조회
    public List<Item> getItemList() {
        List<Item> items = itemDAO.selectItemList();

        // 각 아이템의 이미지 리스트 설정
        for (Item item : items) {
            if (item.getAdditionalImages() != null && !item.getAdditionalImages().isEmpty()) {
                try {
                    List<String> imageUrls = objectMapper.readValue(
                            item.getAdditionalImages(),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
                    );
                    item.setImageUrlList(imageUrls);
                } catch (Exception e) {
                    System.err.println("Error parsing additional images: " + e.getMessage());
                    item.setImageUrlList(new ArrayList<>());
                }
            } else {
                item.setImageUrlList(new ArrayList<>());
            }
        }

        return items;
    }

    // 이미지 URL에서 파일명 추출
    private String getImageNameFromUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }

        if (imageUrl.startsWith("/images/")) {
            return imageUrl.substring("/images/".length());
        } else if (imageUrl.startsWith("/images/")) {
            return imageUrl.substring("/images/".length());
        }

        return imageUrl;
    }

    // 아이템 정보 업데이트
    public Item updateItem(Item item) {
        Item existingItem = itemDAO.selectItem(item.getItemId());
        if (existingItem == null) {
            throw new IllegalArgumentException("Item not found with id: " + item.getItemId());
        }

        // 이미지 URL 유지
        if (item.getImageUrl() == null || item.getImageUrl().isEmpty()) {
            item.setImageUrl(existingItem.getImageUrl());
        }

        // 추가 이미지 유지
        if (item.getAdditionalImages() == null) {
            item.setAdditionalImages(existingItem.getAdditionalImages());
        }

        item.setCreatedAt(existingItem.getCreatedAt());

        itemDAO.updateItem(item);
        return getItemWithImageList(item.getItemId());
    }

    // 아이템 삭제
    public void deleteItem(Long itemId) {
        Item existingItem = itemDAO.selectItem(itemId);
        if (existingItem == null) {
            throw new IllegalArgumentException("Item not found with id: " + itemId);
        }

        // 대표 이미지 삭제
        if (existingItem.getImageUrl() != null && !existingItem.getImageUrl().isEmpty()) {
            String filename = getImageNameFromUrl(existingItem.getImageUrl());
            if (filename != null) {
                try {
                    imageService.deleteImage(filename);
                } catch (IOException e) {
                    System.err.println("Error deleting image: " + e.getMessage());
                }
            }
        }

        // 추가 이미지들 삭제
        if (existingItem.getAdditionalImages() != null && !existingItem.getAdditionalImages().isEmpty()) {
            try {
                List<String> imageUrls = objectMapper.readValue(
                        existingItem.getAdditionalImages(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
                );

                for (String url : imageUrls) {
                    String filename = getImageNameFromUrl(url);
                    if (filename != null) {
                        try {
                            imageService.deleteImage(filename);
                        } catch (IOException e) {
                            System.err.println("Error deleting additional image: " + e.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error parsing additional images: " + e.getMessage());
            }
        }

        itemDAO.deleteItem(itemId);
    }

    // 아이템 이미지 업데이트
    public Item updateItemImage(Long itemId, MultipartFile image) throws IOException {
        List<MultipartFile> images = new ArrayList<>();
        if (image != null && !image.isEmpty()) {
            images.add(image);
        }

        Item existingItem = itemDAO.selectItem(itemId);
        if (existingItem == null) {
            throw new IllegalArgumentException("Item not found with id: " + itemId);
        }

        // 기존 이미지 삭제
        if (existingItem.getImageUrl() != null && !existingItem.getImageUrl().isEmpty()) {
            String filename = getImageNameFromUrl(existingItem.getImageUrl());
            if (filename != null) {
                imageService.deleteImage(filename);
            }
        }

        // 기존 추가 이미지들 삭제
        if (existingItem.getAdditionalImages() != null && !existingItem.getAdditionalImages().isEmpty()) {
            try {
                List<String> imageUrls = objectMapper.readValue(
                        existingItem.getAdditionalImages(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
                );

                for (String url : imageUrls) {
                    String filename = getImageNameFromUrl(url);
                    if (filename != null) {
                        try {
                            imageService.deleteImage(filename);
                        } catch (IOException e) {
                            System.err.println("Error deleting additional image: " + e.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error parsing additional images: " + e.getMessage());
            }
        }

        // 새 이미지 설정
        if (!images.isEmpty()) {
            String imageName = imageService.storeImage(images.get(0));
            existingItem.setImageUrl("/api/images/" + imageName);
        } else {
            existingItem.setImageUrl(null);
        }

        // 추가 이미지 초기화
        existingItem.setAdditionalImages(null);

        itemDAO.updateItem(existingItem);
        return getItemWithImageList(itemId);
    }
}
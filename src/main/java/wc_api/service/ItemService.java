package wc_api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private static final Logger logger = LoggerFactory.getLogger(ItemService.class);

    private final ItemDAO itemDAO;
    private final ImageService imageService;
    private final ObjectMapper objectMapper;

    // 다중 이미지로 아이템 생성
    @Transactional
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
    @Transactional
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

    // 아이템 조회 + 조회수 증가
    @Transactional
    public Item getItemAndIncrementViewCount(Long itemId) {
        // 조회수 증가
        itemDAO.updateViewCount(itemId);

        logger.debug("조회수 증가 시도: {}", itemId);
        // 업데이트된 아이템 정보 가져오기
        return getItemWithImageList(itemId);
    }

    // 아이템 목록 조회
    @Transactional
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
    @Transactional
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
    @Transactional
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
    @Transactional
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

    // 아이템과 다중 이미지를 함께 업데이트
    @Transactional
    public Item updateItemWithImages(Item item, List<MultipartFile> images) throws IOException {
        Item existingItem = itemDAO.selectItem(item.getItemId());
        if (existingItem == null) {
            throw new IllegalArgumentException("Item not found with id: " + item.getItemId());
        }

        // 생성일자 유지
        item.setCreatedAt(existingItem.getCreatedAt());

        // 이미지 관련 처리가 필요한지 확인
        boolean needImageUpdate = (images != null && !images.isEmpty()) ||
                (item.getImageIds() != null && !item.getImageIds().isEmpty());

        if (needImageUpdate) {
            // 기존 이미지 URL 및 추가 이미지 목록 가져오기
            List<String> existingImages = new ArrayList<>();
            if (existingItem.getImageUrl() != null && !existingItem.getImageUrl().isEmpty()) {
                existingImages.add(existingItem.getImageUrl());
            }

            if (existingItem.getAdditionalImages() != null && !existingItem.getAdditionalImages().isEmpty()) {
                try {
                    List<String> additionalImageUrls = objectMapper.readValue(
                            existingItem.getAdditionalImages(),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
                    );
                    existingImages.addAll(additionalImageUrls);
                } catch (Exception e) {
                    System.err.println("Error parsing additional images: " + e.getMessage());
                }
            }

            // 유지할 이미지와 삭제할 이미지 분리
            List<String> imagesToKeep = new ArrayList<>();
            List<String> imagesToDelete = new ArrayList<>();

            // 유지할 이미지 ID 목록이 있는 경우
            if (item.getImageIds() != null && !item.getImageIds().isEmpty()) {
                // 이미지 ID를 기반으로 유지할 이미지 URL 찾기
                for (String existingUrl : existingImages) {
                    String fileName = getImageNameFromUrl(existingUrl);
                    if (item.getImageIds().contains(fileName)) {
                        imagesToKeep.add(existingUrl);
                    } else {
                        imagesToDelete.add(existingUrl);
                    }
                }
            } else {
                // 유지할 이미지 ID 목록이 없으면 모든 기존 이미지 삭제
                imagesToDelete.addAll(existingImages);
                imagesToKeep.clear();
            }

            // 삭제할 이미지 처리
            for (String imageUrl : imagesToDelete) {
                String fileName = getImageNameFromUrl(imageUrl);
                if (fileName != null) {
                    try {
                        imageService.deleteImage(fileName);
                    } catch (IOException e) {
                        System.err.println("Error deleting image: " + e.getMessage());
                    }
                }
            }

            // 새 이미지 저장
            List<String> newImageUrls = new ArrayList<>();
            if (images != null) {
                for (MultipartFile imageFile : images) {
                    if (imageFile != null && !imageFile.isEmpty()) {
                        String imageName = imageService.storeImage(imageFile);
                        newImageUrls.add("/images/" + imageName);
                    }
                }
            }

            // 유지할 이미지와 새 이미지 합치기
            List<String> allImages = new ArrayList<>();
            allImages.addAll(imagesToKeep);
            allImages.addAll(newImageUrls);

            // 이미지가 있는 경우 첫 번째 이미지를 대표 이미지로 설정
            if (!allImages.isEmpty()) {
                item.setImageUrl(allImages.get(0));

                // 나머지 이미지가 있으면 추가 이미지로 설정
                if (allImages.size() > 1) {
                    List<String> additionalImages = allImages.subList(1, allImages.size());
                    item.setAdditionalImages(objectMapper.writeValueAsString(additionalImages));
                } else {
                    item.setAdditionalImages(null);
                }
            } else {
                // 이미지가 없는 경우
                item.setImageUrl(null);
                item.setAdditionalImages(null);
            }
        } else {
            // 이미지 파일이 없는 경우, 기존 이미지 정보 유지
            item.setImageUrl(existingItem.getImageUrl());
            item.setAdditionalImages(existingItem.getAdditionalImages());
        }

        // 아이템 정보 업데이트
        itemDAO.updateItem(item);
        return getItemWithImageList(item.getItemId());
    }

    @Transactional
    public List<Item> getItemsBySellerId(Long sellerId) {
        List<Item> items = itemDAO.selectItemsBySellerId(sellerId);

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
    @Transactional
    public List<Item> getItemsByCategory(Long categoryId) {
        List<Item> items = itemDAO.selectItemsByCategory(categoryId);

        // 각 아이템의 이미지 리스트 설정 (getItemList와 동일한 로직)
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
    // 조회수 높은 순으로 아이템 목록 조회
    @Transactional
    public List<Item> getItemsOrderByViewCount() {
        List<Item> items = itemDAO.selectItemsOrderByViewCount();

        // 각 아이템의 이미지 리스트 설정 (기존 getItemList와 동일한 로직)
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
    // 아이템 상태 변경
    @Transactional
    public Item updateItemStatus(Long itemId, String status, Long userId) {
        // 아이템 존재 여부 확인
        Item existingItem = itemDAO.selectItem(itemId);
        if (existingItem == null) {
            throw new IllegalArgumentException("Item not found with id: " + itemId);
        }

        // 권한 체크 - 본인의 아이템만 수정 가능
        if (!existingItem.getSellerId().equals(userId)) {
            throw new IllegalArgumentException("You can only update your own items");
        }

        // 유효한 상태값인지 체크
        if (!"SELLING".equals(status) && !"SOLD".equals(status)) {
            throw new IllegalArgumentException("Invalid status. Only SELLING or SOLD are allowed");
        }

        // 상태 업데이트
        itemDAO.updateItemStatus(itemId, status);

        // 업데이트된 아이템 반환
        return getItemWithImageList(itemId);
    }
}
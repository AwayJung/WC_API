package wc_api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import wc_api.dao.ItemDAO;
import wc_api.model.db.item.Item;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemDAO itemDAO;

    // 아이템 생성
    public Item createItem(Item item) {
        // 생성 시간 설정
        item.setCreatedAt(LocalDateTime.now());

        // 아이템 생성
        itemDAO.insertItem(item);

        // 생성된 아이템 반환
        return itemDAO.selectItem(item.getItemId());
    }

    // 아이템 상세 조회
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

    // 아이템 수정
    public Item updateItem(Item item) {
        // 기존 아이템 존재 여부 확인
        Item existingItem = itemDAO.selectItem(item.getItemId());

        if (existingItem == null) {
            throw new IllegalArgumentException("Item not found with id: " + item.getItemId());
        }

        // 기존 생성 시간 유지
        item.setCreatedAt(existingItem.getCreatedAt());

        // 아이템 수정
        itemDAO.updateItem(item);

        // 수정된 아이템 반환
        return itemDAO.selectItem(item.getItemId());
    }

    // 아이템 삭제
    public void deleteItem(Long itemId) {
        // 기존 아이템 존재 여부 확인
        Item existingItem = itemDAO.selectItem(itemId);

        if (existingItem == null) {
            throw new IllegalArgumentException("Item not found with id: " + itemId);
        }

        itemDAO.deleteItem(itemId);
    }
}
package wc_api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wc_api.dao.ItemDAO;
import wc_api.dao.ItemLikeDAO;
import wc_api.model.db.item.Item;

import java.util.List;

/**
 * 아이템 좋아요 관련 비즈니스 로직을 처리하는 서비스
 */
@Service
@RequiredArgsConstructor
public class ItemLikeService {

    private final ItemLikeDAO itemLikeDAO;
    private final ItemDAO itemDAO;

    /**
     * 아이템 좋아요 토글
     * @param userId 사용자 ID
     * @param itemId 아이템 ID
     * @return 토글 후 좋아요 상태 (true: 좋아요 추가됨, false: 좋아요 취소됨)
     */
    @Transactional
    public boolean toggleItemLike(Long userId, Long itemId) {
        // 아이템 존재 확인
        Item item = itemDAO.selectItem(itemId);
        if (item == null) {
            throw new IllegalArgumentException("Item not found with id: " + itemId);
        }

        // 이미 좋아요를 했는지 확인
        boolean exists = itemLikeDAO.countItemLike(userId, itemId) > 0;

        if (exists) {
            // 좋아요가 이미 있으면 삭제 (취소)
            itemLikeDAO.deleteItemLike(userId, itemId);
            return false;
        } else {
            // 좋아요가 없으면 추가
            itemLikeDAO.insertItemLike(userId, itemId);
            return true;
        }
    }

    /**
     * 사용자가 좋아요한 아이템 목록 조회
     * @param userId 사용자 ID
     * @return 좋아요한 아이템 목록
     */
    public List<Item> getMyLikedItems(Long userId) {
        // 사용자가 좋아요한 아이템 ID 목록 조회
        List<Long> likedItemIds = itemLikeDAO.selectItemIdsByUserId(userId);

        // 아이템 ID 목록으로 아이템 정보 조회 - 현재는 개별 조회 방식
        // 실제로는 IN 쿼리로 한 번에 가져오는 메서드를 ItemDAO에 추가하는 것이 효율적입니다
        List<Item> likedItems = likedItemIds.stream()
                .map(itemDAO::selectItem)
                .toList();

        // 각 아이템에 좋아요 상태 설정 (이미 목록에 있으므로 모두 true)
        for (Item item : likedItems) {
            item.setIsLiked(true);
            // 각 아이템의 좋아요 수도 설정
            Long likeCount = itemLikeDAO.countItemLikeByItemId(item.getItemId());
            item.setLikeCount(likeCount);
        }

        return likedItems;
    }

    /**
     * 특정 아이템의 좋아요 상태 확인
     * @param userId 사용자 ID
     * @param itemId 아이템 ID
     * @return 좋아요 상태
     */
    public boolean isItemLiked(Long userId, Long itemId) {
        return itemLikeDAO.countItemLike(userId, itemId) > 0;
    }

    /**
     * 특정 아이템의 좋아요 수 조회
     * @param itemId 아이템 ID
     * @return 좋아요 수
     */
    public Long getItemLikeCount(Long itemId) {
        return itemLikeDAO.countItemLikeByItemId(itemId);
    }

    /**
     * 아이템 상세 정보와 좋아요 상태를 함께 조회
     * @param itemId 아이템 ID
     * @param userId 사용자 ID
     * @return 좋아요 상태가 포함된 아이템 정보
     */
    public Item getItemDetailWithLikeStatus(Long itemId, Long userId) {
        // 아이템 정보 조회
        Item item = itemDAO.selectItem(itemId);
        if (item == null) {
            throw new IllegalArgumentException("Item not found with id: " + itemId);
        }

        // 좋아요 상태 설정
        boolean isLiked = isItemLiked(userId, itemId);
        item.setIsLiked(isLiked);

        // 좋아요 수 설정
        Long likeCount = getItemLikeCount(itemId);
        item.setLikeCount(likeCount);

        return item;
    }
}
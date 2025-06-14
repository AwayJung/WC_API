package wc_api.dao;

import org.apache.ibatis.annotations.Mapper;
import wc_api.model.db.item.Item;

import java.util.List;

@Mapper
public interface ItemDAO {
    // 아이템 생성
    void insertItem(Item item);

    // 아이템 상세 조회
    Item selectItem(Long itemId);

    // 아이템 목록 조회
    List<Item> selectItemList();

    // 아이템 수정
    void updateItem(Item item);

    // 아이템 삭제
    void deleteItem(Long itemId);

    // 조회수 증가 메소드 추가
    void updateViewCount(Long itemId);

    // 판매자별 아이템 목록 조회
    List<Item> selectItemsBySellerId(Long sellerId);

    // 카테고리별 아이템 목록 조회
    List<Item> selectItemsByCategory(Long categoryId);

    //조회순별 아이템 목록 조회
    List<Item> selectItemsOrderByViewCount();

    void updateItemStatus(Long itemId, String status);
}
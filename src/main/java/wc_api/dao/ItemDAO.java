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
}
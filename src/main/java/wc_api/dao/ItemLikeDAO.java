package wc_api.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ItemLikeDAO {
    // 좋아요 추가
    void insertItemLike(@Param("userId") Long userId, @Param("itemId") Long itemId);

    // 좋아요 여부 확인
    int countItemLike(@Param("userId") Long userId, @Param("itemId") Long itemId);

    // 아이템의 전체 좋아요 수 조회
    Long countItemLikeByItemId(@Param("itemId") Long itemId);

    // 사용자가 좋아요한 아이템 ID 목록 조회
    List<Long> selectItemIdsByUserId(@Param("userId") Long userId);

    // ItemLikeDAO 인터페이스에 추가
    boolean getItemLikeStatus(Long userId, Long itemId);

    void updateItemLikeStatus(Long userId, Long itemId, boolean status);

    // 레코드 존재 여부 확인 (상태와 상관없이)
    boolean checkRecordExists(Long userId, Long itemId);
}
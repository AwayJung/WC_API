package wc_api.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import wc_api.model.db.chat.ChatMessage;
import wc_api.model.db.chat.ChatRoom;
import wc_api.model.db.chat.ChatRoomUser;

import java.util.List;

@Mapper
public interface ChatDAO {
    /**
     * 새로운 채팅방을 생성
     * @param room 채팅방 정보 (roomId, itemId)
     */
    void createChatRoom(ChatRoom room);

    /**
     * 아이템ID로 판매자ID 조회
     * @param itemId 상품 ID
     * @return 판매자 ID
     */
    int findSellerIdByItemId(int itemId);

    /**
     * 채팅방-사용자 관계 생성
     * @param user 채팅방 사용자 정보 (roomId, userId, userType)
     */
    void createChatRoomUser(ChatRoomUser user);

    /**
     * 동일 아이템에 대한 구매자-판매자 채팅방 조회
     * @param itemId 상품 ID
     * @param buyerId 구매자 ID
     * @return 존재하는 채팅방 정보
     */
    ChatRoom findRoomByItemAndUsers(int itemId, int buyerId);

    /**
     * 사용자의 채팅방 목록 조회
     * @param userId 사용자 ID
     * @return 참여중인 채팅방 목록
     */
    List<ChatRoom> findRoomsByUserId(int userId);

    /**
     * 채팅방 메시지 목록 조회
     * @param roomId 채팅방 ID
     * @return 채팅 메시지 목록
     */
    List<ChatMessage> findMessagesByRoomId(String roomId);

    /**
     * 새 메시지 저장
     * @param message 메시지 정보 (roomId, senderId, content, sentTime)
     */
    void insertMessage(ChatMessage message);

    /**
     * 메시지 읽음 상태 업데이트
     * @param roomId 채팅방 ID
     * @param userId 읽은 사용자 ID
     */
    void updateMessagesReadStatus(String roomId, int userId);

    void insertChatRoomUser(String roomId, int userId, String userType, int messageId);
}
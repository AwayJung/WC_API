package wc_api.dao;

import wc_api.model.db.chat.ChatMessage;
import wc_api.model.db.chat.ChatRoom;
import java.util.List;

public interface ChatDAO {
    /**
     * 새로운 채팅방을 생성
     * @param room 생성할 채팅방 정보
     */
    void createChatRoom(ChatRoom room);

    /**
     * 특정 사용자가 참여중인 모든 채팅방 목록을 조회
     * @param userId 사용자 ID
     * @return 해당 사용자의 채팅방 목록
     */
    List<ChatRoom> findRoomsByUserId(int userId);

    /**
     * 새로운 채팅 메시지를 저장
     * @param message 저장할 채팅 메시지 정보
     */
    void insertMessage(ChatMessage message);

    /**
     * 특정 채팅방의 모든 메시지 목록을 조회
     * @param roomId 채팅방 ID
     * @return 해당 채팅방의 메시지 목록
     */
    List<ChatMessage> findMessagesByRoomId(String roomId);

    /**
     * 메세지 읽음 표시
     * @param roomId
     * @param userId
     */
    void updateMessagesReadStatus(String roomId, int userId);
}
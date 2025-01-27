package wc_api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import wc_api.dao.ChatDAO;
import wc_api.model.db.chat.ChatMessage;
import wc_api.model.db.chat.ChatRoom;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatDAO chatDAO;

    // 채팅방 생성
    public ChatRoom createRoom(Long itemId, int sellerId, int buyerId) {
        ChatRoom room = new ChatRoom();
        room.setRoomId(UUID.randomUUID().toString());  // 랜덤 ID 생성
        room.setItemId(itemId);
        room.setSellerId(sellerId);
        room.setBuyerId(buyerId);

        chatDAO.createChatRoom(room);
        return room;
    }


    // 사용자의 채팅방 목록 조회
    public List<ChatRoom> getRoomList(int userId) {
        return chatDAO.findRoomsByUserId(userId);
    }

    // 채팅방 메시지 상세 조회
    public List<ChatMessage> getRoomMessages(String roomId) {
        return chatDAO.findMessagesByRoomId(roomId);
    }

    // 메세지 전송
    public ChatMessage sendMessage(ChatMessage message) {
        chatDAO.insertMessage(message);
        return message;
    }

    // 메세지 읽음 표시
    public void markMessageAsRead(String roomId, int userId) {
        chatDAO.updateMessagesReadStatus(roomId,userId);
    }
}
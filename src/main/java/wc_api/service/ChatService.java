package wc_api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wc_api.dao.ChatDAO;
import wc_api.model.db.chat.ChatMessage;
import wc_api.model.db.chat.ChatRoom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatDAO chatDAO;

    @Transactional
    public List<ChatRoom> getRoomList(int userId) {
        return chatDAO.findRoomsByUserId(userId);
    }

    @Transactional
    public List<ChatMessage> getRoomMessages(String roomId) {
        return chatDAO.findMessagesByRoomId(roomId);
    }

    @Transactional
    public ChatMessage sendMessage(ChatMessage message, Integer itemId) {
        System.out.println("메소드 시작 - message: " + message);
        System.out.println("받은 userId: " + message.getUserId());
        System.out.println("받은 itemId: " + itemId);

        // 신규 채팅방 생성 여부 확인 및 처리
        if (itemId != null) {
            ChatRoom chatRoom = chatDAO.findRoomByItemAndUsers(itemId, message.getUserId());
            System.out.println("조회된 chatRoom: " + chatRoom);

            if (chatRoom == null) {
                // 새로운 채팅방 생성
                int sellerId = chatDAO.findSellerIdByItemId(itemId);
                System.out.println("조회된 sellerId: " + sellerId);

                ChatRoom newRoom = new ChatRoom();
                newRoom.setRoomId(UUID.randomUUID().toString());
                newRoom.setItemId(itemId);
                chatDAO.createChatRoom(newRoom);
                System.out.println("생성된 newRoom: " + newRoom);

                message.setRoomId(newRoom.getRoomId());
            } else {
                message.setRoomId(chatRoom.getRoomId());
            }
        }

        try {
            message.setSentTime(LocalDateTime.now());
            message.setRead(false);

            if ("TALK".equals(message.getType())) {
                System.out.println("메시지 저장 직전 데이터: " + message);
                chatDAO.insertMessage(message);
                System.out.println("저장된 메시지 ID: " + message.getMessageId());

                if (message.getMessageId() > 0) {
                    System.out.println("chat_room_user 저장 전 데이터:");
                    System.out.println("roomId: " + message.getRoomId());
                    System.out.println("userId: " + message.getUserId());
                    System.out.println("messageId: " + message.getMessageId());

                    // user_type은 SQL에서 자동으로 결정됨
                    chatDAO.insertChatRoomUser(
                            message.getRoomId(),
                            message.getUserId(),
                            null,
                            message.getMessageId()
                    );
                }
            }

            return message;
        } catch (DataIntegrityViolationException e) {
            System.out.println("에러 발생: " + e.getMessage());
            e.printStackTrace();

            if (message.getMessageId() > 0) {
                return message;
            }
            throw e;
        }
    }

    // 메세지 읽음 표시
    @Transactional
    public void markMessageAsRead(String roomId, int userId) {
        chatDAO.updateMessagesReadStatus(roomId, userId);
    }

}
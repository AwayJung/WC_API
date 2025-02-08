package wc_api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wc_api.dao.ChatDAO;
import wc_api.model.db.chat.ChatMessage;
import wc_api.model.db.chat.ChatRoom;
import wc_api.model.db.chat.ChatRoomUser;
import wc_api.model.request.ChatRoomCreateRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatDAO chatDAO;

    // 채팅방 생성
    @Transactional
    public ChatRoom createRoom(ChatRoomCreateRequest request) {
        // 1. 판매자 ID 조회 (item의 seller_id)
        int sellerId = chatDAO.findSellerIdByItemId(request.getItemId());

        ChatRoom existingRoom = chatDAO.findRoomByItemAndUsers(request.getItemId(), request.getBuyerId());
        if (existingRoom != null) {
            return existingRoom;
        }
        // 2. chat_room 생성
        ChatRoom room = new ChatRoom();
        room.setRoomId(UUID.randomUUID().toString());
        room.setItemId(request.getItemId());
        chatDAO.createChatRoom(room);

        // 3. chat_room_user에 구매자와 판매자 추가
        ChatRoomUser buyer = new ChatRoomUser();
        buyer.setRoomId(room.getRoomId());
        buyer.setUserId(request.getBuyerId());
        buyer.setUserType("B");
        chatDAO.createChatRoomUser(buyer);

        ChatRoomUser seller = new ChatRoomUser();
        seller.setRoomId(room.getRoomId());
        seller.setUserId(sellerId);
        seller.setUserType("S");
        chatDAO.createChatRoomUser(seller);

        return room;
    }

    // 사용자의 채팅방 목록 조회
    @Transactional
    public List<ChatRoom> getRoomList(int userId) {
        return chatDAO.findRoomsByUserId(userId);
    }

    // 채팅방 메시지 상세 조회
    @Transactional
    public List<ChatMessage> getRoomMessages(String roomId) {
        return chatDAO.findMessagesByRoomId(roomId);
    }

    // 메세지 전송
    @Transactional
    public ChatMessage sendMessage(ChatMessage message) {
        // 시스템 메시지(JOIN, LEAVE)는 DB 저장하지 않고 바로 반환
        if ("JOIN".equals(message.getType()) || "LEAVE".equals(message.getType())) {
            message.setSentTime(LocalDateTime.now());
            return message;
        }

        try {
            System.out.println("Before insert - message: " + message);
            message.setSentTime(LocalDateTime.now());
            message.setRead(false);

            // TALK 타입일 때만 DB에 저장
            if ("TALK".equals(message.getType())) {
                chatDAO.insertMessage(message);
                System.out.println("After insert - messageId: " + message.getMessageId());
                chatDAO.linkMessageToRoom(message.getMessageId(), message.getRoomId());
            }

            return message;
        } catch (DataIntegrityViolationException e) {
            // 이미 DB에 성공적으로 들어갔다면 예외를 무시
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
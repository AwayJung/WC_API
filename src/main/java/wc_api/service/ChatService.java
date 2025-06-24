package wc_api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wc_api.dao.ChatDAO;
import wc_api.model.db.chat.ChatMessage;
import wc_api.model.db.chat.ChatRoom;
import wc_api.model.db.chat.ChatRoomUser;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatDAO chatDAO;

    @Transactional
    public List<ChatRoom> getRoomList(int userId) {
        try {
            List<ChatRoom> rooms = chatDAO.findRoomsByUserId(userId);
            return rooms != null ? rooms : new ArrayList<>();
        } catch (Exception e) {
            System.out.println("getRoomList 에러 발생: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Transactional
    public List<ChatMessage> getRoomMessages(String roomId) {
        return chatDAO.findMessagesByRoomId(roomId);
    }

    @Transactional
    public ChatMessage sendMessage(ChatMessage message, Integer itemId) {
        try {
            System.out.println("메시지 전송 시작 - userId: " + message.getUserId());
            String roomId = message.getRoomId(); // 클라이언트에서 전달받은 roomId

            // roomId 유효성 검사
            if (roomId == null || roomId.trim().isEmpty()) {
                throw new IllegalArgumentException("유효한 채팅방 ID가 필요합니다");
            }

            // 채팅방 존재 여부 검증
            ChatRoom existingRoom = chatDAO.findRoomById(roomId);
            if (existingRoom == null) {
                throw new IllegalArgumentException("존재하지 않는 채팅방입니다: " + roomId);
            }

            // 메시지 정보 설정
            message.setSentTime(LocalDateTime.now());
            message.setRead(false);

            // 메시지 저장
            if ("TALK".equals(message.getType())) {
                // 메시지 저장 전 로그
                System.out.println("메시지 저장 전 준비 - roomId: " + roomId);

                chatDAO.insertMessage(message);
                System.out.println("메시지 저장 완료 - ID: " + message.getMessageId());

                // 저장된 메시지의 ID 확인
                int messageId = message.getMessageId();

                // chat_room_user 업데이트 전 현재 상태 확인
                System.out.println("===== chat_room_user 업데이트 전 =====");
                System.out.println("메시지 ID: " + messageId);

                // 이 시점에서 해당 messageId로 저장된 chat_room_user 레코드 개수 확인
//                 chatDAO에 countByMessageId 메서드가 없다면 추가해야 합니다
                int beforeCount = chatDAO.countByMessageId(messageId);
                System.out.println("저장 전 chat_room_user 레코드 개수: " + beforeCount);

                if (messageId > 0) {
                    // 기존 user_type 조회
                    String userType = chatDAO.findUserTypeInRoom(roomId, message.getUserId());
                    System.out.println("사용자 타입 조회 결과: " + userType);

                    // insertChatRoomUser 호출 직전
                    System.out.println("insertChatRoomUser 호출 - roomId: " + roomId
                            + ", userId: " + message.getUserId()
                            + ", userType: " + userType
                            + ", messageId: " + messageId);

                    chatDAO.insertChatRoomUser(
                            roomId,
                            message.getUserId(),
                            userType,  // 기존 타입 유지
                            messageId
                    );

                    // insertChatRoomUser 호출 직후
                    System.out.println("insertChatRoomUser 호출 완료");

                    int afterCount = chatDAO.countByMessageId(messageId);
                    System.out.println("저장 후 chat_room_user 레코드 개수: " + afterCount);
                    System.out.println("증가된 레코드 수: " + (afterCount - beforeCount));
                }
            }

            return message;
        } catch (Exception e) {
            System.out.println("메시지 전송 중 에러: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Transactional
    public void markMessageAsRead(String roomId, int userId) {
        chatDAO.updateMessagesReadStatus(roomId, userId);
    }

    @Transactional
    public String createOrGetChatRoom(Integer itemId, Integer userId) {
        try {
            System.out.println("채팅방 생성/조회 시작 - itemId: " + itemId + ", userId: " + userId);

            // 기존 채팅방이 있는지 확인
            ChatRoom existingRoom = chatDAO.findRoomByItemAndUsers(itemId, userId);

            if (existingRoom != null) {
                System.out.println("기존 채팅방 발견 - roomId: " + existingRoom.getRoomId());
                return existingRoom.getRoomId();
            }
            // 새로운 채팅방 생성 로직
            int sellerId = chatDAO.findSellerIdByItemId(itemId);
            System.out.println("판매자 ID: " + sellerId);

            // 판매자 체크를 제거하고 항상 채팅방 생성
            String roomId = UUID.randomUUID().toString();
            ChatRoom newRoom = new ChatRoom();
            newRoom.setRoomId(roomId);
            newRoom.setItemId(itemId);
            chatDAO.createChatRoom(newRoom);
            System.out.println("새 채팅방 생성 완료 - roomId: " + roomId);

            // 사용자 역할 결정 (판매자인지 구매자인지)
            String userType;
            if (userId.equals(sellerId)) {
                userType = "S"; // 판매자
                System.out.println("사용자는 판매자입니다 - userId: " + userId);

                // 구매자가 없는 상태이므로 자신만 채팅방에 추가
                ChatRoomUser sellerUser = new ChatRoomUser();
                sellerUser.setRoomId(roomId);
                sellerUser.setUserId(userId);
                sellerUser.setUserType(userType);
                chatDAO.createChatRoomUser(sellerUser);
                System.out.println("판매자 정보 저장 완료 - userId: " + userId);
            } else {
                // 구매자인 경우
                userType = "B";
                System.out.println("사용자는 구매자입니다 - userId: " + userId);

                // 구매자 정보 저장
                ChatRoomUser buyerUser = new ChatRoomUser();
                buyerUser.setRoomId(roomId);
                buyerUser.setUserId(userId);
                buyerUser.setUserType("B");
                chatDAO.createChatRoomUser(buyerUser);
                System.out.println("구매자 정보 저장 완료 - userId: " + userId);

                // 판매자 정보 저장
                ChatRoomUser sellerUser = new ChatRoomUser();
                sellerUser.setRoomId(roomId);
                sellerUser.setUserId(sellerId);
                sellerUser.setUserType("S");
                chatDAO.createChatRoomUser(sellerUser);
                System.out.println("판매자 정보 저장 완료 - userId: " + sellerId);
            }

            return roomId;
        } catch (Exception e) {
            System.out.println("채팅방 생성 중 에러 발생: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 채팅방 삭제
     * @param roomId 삭제할 채팅방 ID
     * @throws IllegalArgumentException 채팅방이 존재하지 않을 때
     */
    @Transactional
    public void deleteChatRoom(String roomId) {
        try {
            // 채팅방 존재 여부 확인
            ChatRoom existingRoom = chatDAO.findRoomById(roomId);
            if (existingRoom == null) {
                throw new IllegalArgumentException("존재하지 않는 채팅방입니다.");
            }

            // 관련 데이터 삭제 (외래키 제약조건 순서대로)
            chatDAO.deleteChatRoomUsersByRoomId(roomId);
            chatDAO.deleteMessagesByRoomId(roomId);
            chatDAO.deleteChatRoom(roomId);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("채팅방 삭제 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 사용자가 참여한 총 채팅방 개수 조회
     * @param userId 사용자 ID
     * @return 참여한 채팅방 총 개수
     */
    @Transactional(readOnly = true)
    public int getChatRoomCountByUserId(int userId) {
        return chatDAO.countChatRoomsByUserId(userId);
    }
}
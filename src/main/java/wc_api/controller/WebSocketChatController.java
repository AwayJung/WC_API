package wc_api.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.web.bind.annotation.RestController;
import wc_api.model.db.chat.ChatMessage;
import wc_api.service.ChatService;

@Slf4j
@RestController
public class WebSocketChatController {
    private final ChatService chatService;

    public WebSocketChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @MessageMapping("/chat/{roomId}")
    @SendTo("/topic/chat/{roomId}")
    public ChatMessage handleMessage(
            @DestinationVariable(value = "roomId") String roomId,
            @Payload ChatMessage message,
            @Header(value = "itemId", required = false) Integer itemId
    ) throws Exception {
        log.debug("채팅 메시지 수신 - roomId: {}, userId: {}, itemId: {}", roomId, message.getUserId(), itemId);

        // "null" 문자열이나 빈 문자열인 경우 예외 처리
        if (roomId == null || roomId.equals("null") || roomId.trim().isEmpty()) {
            throw new IllegalArgumentException("유효한 채팅방 ID가 필요합니다");
        }

        // roomId 설정
        message.setRoomId(roomId);

        // 시스템 메시지 처리
        if ("JOIN".equals(message.getType()) || "LEAVE".equals(message.getType())) {
            return message;
        }

        // 일반 메시지 처리
        ChatMessage sentMessage = chatService.sendMessage(message, itemId);

        return sentMessage;
    }

    @MessageMapping("/room/{roomId}/read")
    public void markAsRead(@DestinationVariable String roomId, int userId) throws Exception {
        if (roomId == null || roomId.equals("null") || roomId.trim().isEmpty()) {
            log.warn("잘못된 roomId로 읽음 표시 요청: {}", roomId);
            return;
        }

        log.debug("읽음 표시 - Room ID: {}, User ID: {}", roomId, userId);
        chatService.markMessageAsRead(roomId, userId);
    }
}
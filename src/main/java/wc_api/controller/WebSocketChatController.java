package wc_api.controller;

import lombok.extern.slf4j.Slf4j;  // 추가
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RestController;
import wc_api.model.db.chat.ChatMessage;
import wc_api.service.ChatService;

@Slf4j  // 추가
@RestController
public class WebSocketChatController {
    private final ChatService chatService;

    public WebSocketChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @MessageMapping("/chat/{roomId}")
    @SendTo("/topic/chat/{roomId}")
    public ChatMessage handleMessage(@DestinationVariable String roomId, ChatMessage message) throws Exception {
        message.setRoomId(roomId);

        // 시스템 메시지(JOIN, LEAVE 등)와 일반 메시지 구분
        if ("JOIN".equals(message.getType()) || "LEAVE".equals(message.getType())) {
            // 시스템 메시지는 DB 저장 없이 바로 반환
            return message;
        }

        // 일반 메시지는 기존대로 처리
        return chatService.sendMessage(message);
    }

    @MessageMapping("/room/{roomId}/read")
    public void markAsRead(@DestinationVariable String roomId, int userId) throws Exception {
        log.debug("Mark as read - Room ID: {}, User ID: {}", roomId, userId);
        chatService.markMessageAsRead(roomId, userId);
    }
}
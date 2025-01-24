package wc_api.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import wc_api.model.db.chat.ChatMessage;
import wc_api.service.ChatService;

@Controller
public class WebSocketChatController {
    private final ChatService chatService;

    public WebSocketChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @MessageMapping("/chat/{roomId}")
    @SendTo("/topic/chat/{roomId}")
    public ChatMessage handleMessage(@DestinationVariable String roomId, ChatMessage message) throws Exception {
        return chatService.sendMessage(message);
    }

    @MessageMapping("/room/{roomId}/read")
    public void markAsRead(@DestinationVariable String roomId, String userId) throws Exception {
        chatService.markMessageAsRead(roomId, userId);
    }
}

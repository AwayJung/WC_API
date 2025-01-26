//package wc_api.common.handler;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.web.socket.TextMessage;
//import org.springframework.web.socket.WebSocketSession;
//import org.springframework.web.socket.handler.TextWebSocketHandler;
//import wc_api.model.db.chat.ChatMessage;
//import wc_api.model.request.MessageRequest;
//import wc_api.service.ChatService;
//
//import java.io.IOException;
//import java.util.Set;
//import java.util.Collections;
//import java.util.HashSet;
//
//public class ChatHandler extends TextWebSocketHandler {
//    private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());
//    private final ChatService chatService;
//    private final ObjectMapper objectMapper;
//
//    public ChatHandler(ChatService chatService, ObjectMapper objectMapper) {
//        this.chatService = chatService;
//        this.objectMapper = objectMapper;
//    }
//
//    @Override
//    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
//        MessageRequest request = objectMapper.readValue(message.getPayload(), MessageRequest.class);
//
//        switch (request.getType()) {
//            case "CHAT":
//                ChatMessage chatMessage = objectMapper.convertValue(request.getData(), ChatMessage.class);
//                ChatMessage savedMessage = chatService.sendMessage(chatMessage);
//                broadcastToRoom(request.getRoomId(), savedMessage);
//                break;
//            case "READ":
//                chatService.markMessageAsRead(request.getRoomId(), request.getUserId());
//                break;
//        }
//    }
//
//    private void broadcastToRoom(String roomId, Object message) throws IOException {
//        String messageStr = objectMapper.writeValueAsString(message);
//        for (WebSocketSession sess : sessions) {
//            if (sess.isOpen()) {
//                sess.sendMessage(new TextMessage(messageStr));
//            }
//        }
//    }
//}
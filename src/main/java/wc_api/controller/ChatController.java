package wc_api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import wc_api.common.constant.ApiRespPolicy;
import wc_api.common.model.response.ApiResp;
import wc_api.model.db.chat.ChatMessage;
import wc_api.model.db.chat.ChatRoom;
import wc_api.service.ChatService;

import java.util.List;
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    // 채팅방 생성
    @PostMapping("/rooms")
    public ResponseEntity<ApiResp> createRoom(@RequestBody ChatRoom request) {
        ChatRoom room = chatService.createRoom(
                request.getItemId(),
                request.getSellerId(),
                request.getBuyerId()
        );
        return ResponseEntity
                .status(ApiRespPolicy.SUCCESS_CREATED.getHttpStatus())
                .body(ApiResp.of(ApiRespPolicy.SUCCESS_CREATED, room));
    }

    // 채팅방 조회
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<ApiResp> getRoom(@PathVariable String roomId) {
        ChatRoom room = chatService.getRoom(roomId);
        return ResponseEntity
                .status(ApiRespPolicy.SUCCESS.getHttpStatus())
                .body(ApiResp.of(ApiRespPolicy.SUCCESS, room));
    }

    // 사용자의 채팅방 목록 조회
    @GetMapping("/rooms/user/{userId}")
    public ResponseEntity<ApiResp> getRoomList(@PathVariable String userId) {
        List<ChatRoom> rooms = chatService.getRoomList(userId);
        return ResponseEntity
                .status(ApiRespPolicy.SUCCESS.getHttpStatus())
                .body(ApiResp.of(ApiRespPolicy.SUCCESS, rooms));
    }

    // 채팅방 메시지 목록 조회
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<ApiResp> getRoomMessages(@PathVariable String roomId) {
        List<ChatMessage> messages = chatService.getRoomMessages(roomId);
        return ResponseEntity
                .status(ApiRespPolicy.SUCCESS.getHttpStatus())
                .body(ApiResp.of(ApiRespPolicy.SUCCESS, messages));
    }

    // 메시지 전송
    @PostMapping("/rooms/{roomId}/messages")
    public ResponseEntity<ApiResp> sendMessage(
            @PathVariable String roomId,
            @RequestBody ChatMessage message) {
        message.setRoomId(roomId);
        ChatMessage savedMessage = chatService.sendMessage(message);
        return ResponseEntity
                .status(ApiRespPolicy.SUCCESS.getHttpStatus())
                .body(ApiResp.of(ApiRespPolicy.SUCCESS, savedMessage));
    }
}
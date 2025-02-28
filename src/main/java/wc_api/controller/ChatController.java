package wc_api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import wc_api.common.constant.ApiRespPolicy;
import wc_api.common.model.response.ApiResp;
import wc_api.model.db.chat.ChatMessage;
import wc_api.model.db.chat.ChatRoom;
import wc_api.service.ChatService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    /**
     * 사용자의 채팅방 목록 조회
     * userId가 참여하는 모든 채팅방 목록 조회(채팅방의 기본 정보를 보여줌)
     * @param userId
     */
    @GetMapping("/rooms/user/{userId}")
    public ResponseEntity<ApiResp> getRoomList(@PathVariable int userId) {
        List<ChatRoom> rooms = chatService.getRoomList(userId);
        return ResponseEntity
                .status(ApiRespPolicy.SUCCESS.getHttpStatus())
                .body(ApiResp.of(ApiRespPolicy.SUCCESS, rooms));
    }

    /**
     * 채팅방 메시지 내역 조회
     * roomId의 메세지 목록을 조회(채팅방 안의 메세지 내용, 사람 등 세부 정보 반환)
     * 사용자가 특정 채팅방에 들어갔을 때 대화 내용 확인 가능
     * @param roomId
     */
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<ApiResp> getRoomMessages(@PathVariable String roomId) {
        List<ChatMessage> messages = chatService.getRoomMessages(roomId);
        return ResponseEntity
                .status(ApiRespPolicy.SUCCESS.getHttpStatus())
                .body(ApiResp.of(ApiRespPolicy.SUCCESS, messages));
    }

    /**
     * 채팅방 생성 또는 기존 채팅방 조회
     * 상품 ID와 사용자 ID를 받아서 채팅방을 생성하거나 기존 채팅방을 반환
     * @param request itemId와 userId를 포함한 요청 객체
     * @return 생성되거나 조회된 채팅방 ID
     */
    @PostMapping("/create-room")
    public ResponseEntity<ApiResp> createChatRoom(@RequestBody Map<String, Object> request) {
        Integer itemId = (Integer) request.get("itemId");
        Integer userId = (Integer) request.get("userId");

        if (itemId == null || userId == null) {
            return ResponseEntity
                    .status(ApiRespPolicy.BAD_REQUEST.getHttpStatus())
                    .body(ApiResp.of(ApiRespPolicy.BAD_REQUEST, "itemId와 userId는 필수 항목입니다."));
        }

        String roomId = chatService.createOrGetChatRoom(itemId, userId);

        Map<String, String> response = new HashMap<>();
        response.put("roomId", roomId);

        return ResponseEntity
                .status(ApiRespPolicy.SUCCESS.getHttpStatus())
                .body(ApiResp.of(ApiRespPolicy.SUCCESS, response));
    }
}
package wc_api.dao;

import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;
import wc_api.model.db.chat.ChatMessage;
import wc_api.model.db.chat.ChatRoom;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ChatDAO {
    private final SqlSession sqlSession;

    // 채팅방 생성
    public void createChatRoom(ChatRoom room) {
        sqlSession.insert("ChatMapper.createChatRoom", room);
    }

    // 채팅방 ID로 조회
    public ChatRoom findRoomById(String roomId) {
        return sqlSession.selectOne("ChatMapper.findRoomById", roomId);
    }

    // 사용자의 채팅방 목록 조회
    public List<ChatRoom> findRoomsByUserId(String userId) {
        return sqlSession.selectList("ChatMapper.findRoomsByUserId", userId);
    }

    // 메시지 저장
    public void insertMessage(ChatMessage message) {
        sqlSession.insert("ChatMapper.insertMessage", message);
    }

    // 채팅방의 메시지 목록 조회
    public List<ChatMessage> findMessagesByRoomId(String roomId) {
        return sqlSession.selectList("ChatMapper.findMessagesByRoomId", roomId);
    }
}

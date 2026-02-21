package com.sila.modules.chat.services;

import com.sila.modules.chat.dto.ChatRoomResponse;
import com.sila.modules.chat.model.ChatRoom;
import com.sila.share.dto.req.PaginationRequest;
import com.sila.share.pagination.EntityResponseHandler;

import java.util.Optional;

public interface ChatRoomService {

    public static String generateRoom(String roomId) {
        var users = roomId.split("_");
        Long senderId = Long.parseLong(users[0]);
        Long receiverId = Long.parseLong(users[1]);
        return senderId < receiverId
                ? senderId + "_" + receiverId
                : receiverId + "_" + senderId;
    }

    ChatRoomResponse createOrGet(Long senderId, Long receiverId);

    ChatRoom findById(Long chatRoomId);

    Optional<ChatRoom> findByRoomId(String roomId);

    String deleteAllRoom();

    String deleteAllRoomByUser();

    EntityResponseHandler<ChatRoomResponse> findAllByMember(PaginationRequest request);

}

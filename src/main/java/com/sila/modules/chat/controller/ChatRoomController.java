package com.sila.modules.chat.controller;

import com.sila.modules.chat.dto.ChatRoomResponse;
import com.sila.modules.chat.repository.ChatRoomRepository;
import com.sila.modules.chat.services.ChatRoomService;
import com.sila.share.dto.req.PaginationRequest;
import com.sila.share.pagination.EntityResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/chats-room")
public class ChatRoomController {
    private final ChatRoomService chatRoomService;
    private final ChatRoomRepository chatRoomRepository;

    @PostMapping()
    public ResponseEntity<ChatRoomResponse> createOrGetRoom(@RequestParam Long senderId, @RequestParam Long receiverId) {
        return ResponseEntity.ok(chatRoomService.createOrGet(senderId, receiverId));
    }

    @GetMapping()
    public ResponseEntity<EntityResponseHandler<ChatRoomResponse>> listAllRooms(@ModelAttribute PaginationRequest request) {
        return ResponseEntity.ok(chatRoomService.findAllByMember(request));
    }

    @Transactional
    @DeleteMapping("/bulk")
    public ResponseEntity<String> deleteAllRoom() {
        return ResponseEntity.ok(chatRoomService.deleteAllRoom());
    }

    @Transactional
    @DeleteMapping("/bulk/user")
    public ResponseEntity<String> deleteAllRoomByUser() {
        return ResponseEntity.ok(chatRoomService.deleteAllRoomByUser());
    }

}

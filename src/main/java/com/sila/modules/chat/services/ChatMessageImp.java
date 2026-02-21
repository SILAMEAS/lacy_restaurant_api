package com.sila.modules.chat.services;

import com.sila.config.exception.NotFoundException;
import com.sila.modules.chat.dto.ChatMessageDTO;
import com.sila.modules.chat.model.ChatMessage;
import com.sila.modules.chat.model.ChatRoom;
import com.sila.modules.chat.repository.ChatMessageRepository;
import com.sila.modules.chat.repository.ChatRoomRepository;
import com.sila.modules.profile.model.User;
import com.sila.modules.profile.services.UserService;
import com.sila.share.dto.req.PaginationRequest;
import com.sila.share.pagination.EntityResponseHandler;
import com.sila.share.pagination.PageableUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatMessageImp implements ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ModelMapper modelMapper;
    private final UserService userService;

    @Override
    public ChatMessageDTO createMessage(ChatMessageDTO request) {
        User sender = userService.getById(request.getSenderId());
        String roomId = ChatRoomService.generateRoom(request.getRoomId());

        ChatRoom room = chatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));
        ChatMessage cartMessage = ChatMessage.builder()
                .content(request.getContent())
                .room(room)
                .timestamp(request.getTimestamp())
                .sender(sender)
                .build();
        chatMessageRepository.save(cartMessage);

        return this.modelMapper.map(cartMessage, ChatMessageDTO.class);

    }

    @Override
    public EntityResponseHandler<ChatMessageDTO> findAll(String roomId, PaginationRequest request) {
        var room = chatRoomRepository.findByRoomId(ChatRoomService.generateRoom(roomId)).orElseThrow(() -> new NotFoundException("Chat Room not found"));
        Pageable pageable = PageableUtil.fromRequest(request);
        return new EntityResponseHandler<>(chatMessageRepository.findAllByRoom(pageable, room).map(re -> this.modelMapper.map(re, ChatMessageDTO.class)));
    }


}

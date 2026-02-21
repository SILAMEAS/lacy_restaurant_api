package com.sila.modules.chat.services;


import com.sila.modules.chat.dto.ChatMessageDTO;
import com.sila.share.dto.req.PaginationRequest;
import com.sila.share.pagination.EntityResponseHandler;

public interface ChatMessageService {

    ChatMessageDTO createMessage(ChatMessageDTO request);

    EntityResponseHandler<ChatMessageDTO> findAll(String roomId, PaginationRequest request);


}

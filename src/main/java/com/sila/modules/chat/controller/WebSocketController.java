package com.sila.modules.chat.controller;

import com.sila.modules.chat.dto.AlertDTO;
import com.sila.modules.chat.dto.ChatMessageDTO;
import com.sila.modules.chat.dto.NotificationDTO;
import com.sila.modules.chat.services.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatMessageService chatMessageService;

    // 🔹 Chat Message Handler
    @MessageMapping("/chat/send")
    public void sendMessage(@Payload ChatMessageDTO dto) {
        // Explicitly send to the topic
        messagingTemplate.convertAndSend("/topic/messages", chatMessageService.createMessage(dto));

    }

    // 🔹 Notification Handler
    @MessageMapping("/notification/send")
    public void sendNotification(@Payload NotificationDTO dto) {
        messagingTemplate.convertAndSend("/topic/notification/" + dto);
    }

    // 🔹 Alert Broadcast
    @MessageMapping("/alert/send")
    public void sendAlert(@Payload AlertDTO dto) {
        messagingTemplate.convertAndSend("/topic/alert", dto);
    }
}

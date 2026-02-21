package com.sila.modules.chat.repository;

import com.sila.modules.chat.model.ChatRoom;
import com.sila.modules.profile.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByRoomId(String roomId);

    Optional<ChatRoom> findById(Long id);

    Set<ChatRoom> findAllByMembers(Set<User> members);

    Page<ChatRoom> findAllByMembers(Pageable pageable, Set<User> members);
}

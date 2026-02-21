package com.sila.config.startup;

import com.sila.modules.profile.model.User;
import com.sila.modules.profile.repository.UserRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class UserMigrateStartup implements ApplicationListener<ApplicationReadyEvent> {

    private final UserRepository userRepository;

    public UserMigrateStartup(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private static LocalDateTime getCurrentDateTime() {
        return LocalDateTime.now();
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        List<User> users = userRepository.findAllByCreatedAtIsNullOrUpdatedAtIsNull();
        if (CollectionUtils.isEmpty(users)) {
            return;
        }
        List<User> toUpdateUsers = new ArrayList<>();
        for (User user : users) {
            boolean hasNullValue = false;
            if (user.getCreatedAt() == null) {
                user.setCreatedAt(getCurrentDateTime());
                hasNullValue = true;
            }
            if (user.getUpdatedAt() == null) {
                user.setUpdatedAt(getCurrentDateTime());
                hasNullValue = true;
            }
            if (hasNullValue) {
                toUpdateUsers.add(user);
            }
        }
        if (!CollectionUtils.isEmpty(toUpdateUsers)) {
            userRepository.saveAll(toUpdateUsers);
        }
    }
}

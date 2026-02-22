package com.sila.config.startup.Imp;

import com.sila.modules.food.repository.FoodRepository;
import com.sila.modules.profile.model.User;
import com.sila.modules.profile.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class UserStartupImp {
    final UserRepository userRepository;

    public UserStartupImp(FoodRepository foodRepository, UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private static LocalDateTime getCurrentDateTime() {
        return LocalDateTime.now();
    }

    public void userMigrate() {
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

package com.sila.config.context;

import com.sila.config.exception.BadRequestException;
import com.sila.modules.profile.model.User;

import java.util.Objects;
import java.util.Optional;

public class UserContext {

    private static final ThreadLocal<User> currentUser = new ThreadLocal<>();

    public static User getUser() {
        if (Objects.isNull(currentUser.get())) {
            throw new BadRequestException("You are not logged in can't get user; note : UserContext");
        }
        return currentUser.get();
    }

    public static void setUser(User user) {
        currentUser.set(user);
    }

    public static Optional<User> findUser() {
        return Optional.ofNullable(currentUser.get());
    }

    public static void clear() {
        currentUser.remove();
    }
}

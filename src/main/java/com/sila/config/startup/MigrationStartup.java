package com.sila.config.startup;

import com.sila.config.startup.Imp.FoodStartupImp;
import com.sila.config.startup.Imp.UserStartupImp;
import lombok.NonNull;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class MigrationStartup implements ApplicationListener<ApplicationReadyEvent> {
    final FoodStartupImp migrationStartupImp;
    final UserStartupImp userStartupImp;

    public MigrationStartup(FoodStartupImp migrationStartupImp, UserStartupImp userStartupImp) {
        this.migrationStartupImp = migrationStartupImp;
        this.userStartupImp = userStartupImp;
    }


    @Override
    public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
//        migrate food
        migrationStartupImp.foodMigrate();
//        migrate user
        userStartupImp.userMigrate();

    }
}
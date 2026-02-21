package com.sila.modules.dashboard.controller;

import com.sila.modules.dashboard.dto.res.DashboardResponse;
import com.sila.modules.dashboard.services.DashboardService;
import com.sila.share.annotation.PreAuthorization;
import com.sila.share.enums.ROLE;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard")
@RequiredArgsConstructor
public class DashboardController {
    final DashboardService dashboardService;

    @PreAuthorization({ROLE.OWNER, ROLE.ADMIN})
    @GetMapping
    public ResponseEntity<DashboardResponse> getAdminDashboard() {
        return new ResponseEntity<>(dashboardService.overviews(), HttpStatus.OK);
    }

    private Map<String, Object> createCard(String id, String title, long value) {
        Map<String, Object> card = new HashMap<>();
        card.put("id", id);
        card.put("title", title);
        card.put("value", value);
        return card;
    }
}

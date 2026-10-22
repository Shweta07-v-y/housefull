
package com.ccm.housefull.dashboard;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Exposes dashboard statistics through a REST API.
@RestController
@RequestMapping("/api/events")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    // GET /api/events/{eventId}/dashboard
    @GetMapping("/{eventId}/dashboard")
    public ResponseEntity<DashboardResponse> getEventDashboard(
            @PathVariable Long eventId) {

        // Ask the service to calculate statistics for this event.
        DashboardResponse response =
                dashboardService.getEventDashboard(eventId);

        // Return the dashboard summary as JSON.
        return ResponseEntity.ok(response);
    }
}

package com.bananabill.controller;

import com.bananabill.model.Bill;
import com.bananabill.service.BillService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final BillService billService;

    public DashboardController(BillService billService) {
        this.billService = billService;
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // Get today's bills count
        Long todayCount = billService.getTodayBillsCount();
        stats.put("todayBills", todayCount);

        // Get total bills count
        Long totalCount = billService.getTotalBillsCount();
        stats.put("totalBills", totalCount);

        // Get recent bills
        List<Bill> recentBills = billService.getRecentBills(10);
        stats.put("recentBills", recentBills);

        return ResponseEntity.ok(stats);
    }
}

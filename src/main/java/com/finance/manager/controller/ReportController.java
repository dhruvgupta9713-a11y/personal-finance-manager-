package com.finance.manager.controller;

import com.finance.manager.dto.response.ReportResponse;
import com.finance.manager.service.ReportService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// controller for generating financial reports
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // get report for a specific month
    @GetMapping("/monthly/{year}/{month}")
    public ResponseEntity<ReportResponse> getMonthlyReport(@PathVariable int year,
                                                            @PathVariable int month,
                                                            HttpServletRequest request) {
        ReportResponse report = reportService.getMonthlyReport(year, month, request);
        return ResponseEntity.ok(report);
    }

    // get report for the whole year
    @GetMapping("/yearly/{year}")
    public ResponseEntity<ReportResponse> getYearlyReport(@PathVariable int year,
                                                           HttpServletRequest request) {
        ReportResponse report = reportService.getYearlyReport(year, request);
        return ResponseEntity.ok(report);
    }
}

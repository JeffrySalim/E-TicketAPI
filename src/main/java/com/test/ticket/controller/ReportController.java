package com.test.ticket.controller;

import com.test.ticket.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final TicketService ticketService;

    @GetMapping("/event/{eventId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getTicketSalesReport(@PathVariable Long eventId) {
        Integer totalSold = ticketService.getTotalSoldTickets(eventId);

        return ResponseEntity.ok(Map.of(
                "message", "Laporan penjualan tiket ditarik",
                "data", Map.of(
                        "eventId", eventId,
                        "totalTicketsSold", totalSold
                )
        ));
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getGlobalSummary() {
        Map<String, Object> report = ticketService.getSummaryReport();

        return ResponseEntity.ok(Map.of(
                "message", "Laporan ringkasan seluruh event berhasil ditarik",
                "data", report
        ));
    }
}

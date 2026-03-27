package com.test.ticket.controller;

import com.test.ticket.dto.Ticket.TicketRequest;
import com.test.ticket.dto.Ticket.TicketResponse;
import com.test.ticket.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@Valid @RequestBody TicketRequest request) {
        TicketResponse data = ticketService.checkoutTicket(request);

        return ResponseEntity.ok(Map.of(
                "message", "Checkout berhasil",
                "data", data
        ));
    }

    @GetMapping("/my-tickets")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<?> getMyTickets() {
        List<TicketResponse> data = ticketService.getAllTickets();

        return ResponseEntity.ok(Map.of(
                "message", "Data tiket berhasil diambil",
                "data", data
        ));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<?> getTicketDetail(@PathVariable Long id) {
        TicketResponse data = ticketService.getTicketById(id);

        return ResponseEntity.ok(Map.of(
                "message", "Detail tiket berhasil diambil",
                "data", data
        ));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<?> cancelTicket(@PathVariable Long id) {
        TicketResponse data = ticketService.cancelTicket(id);
        return ResponseEntity.ok(Map.of(
                "message", "Tiket berhasil dibatalkan",
                "data", data
        ));
    }

}

package com.test.ticket.controller;

import com.test.ticket.dto.Event.EventRequest;
import com.test.ticket.dto.Event.EventResponse;
import com.test.ticket.dto.Pagination.PaginationResponse;
import com.test.ticket.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<?> getAllEvents(
            @RequestParam(value = "page", defaultValue = "0", required = false) int page,
            @RequestParam(value = "size", defaultValue = "5", required = false) int size
    ) {
        PaginationResponse<EventResponse> paginatedData = eventService.getAllEvents(page, size);

        return ResponseEntity.ok(Map.of(
                "message", "Berhasil mengambil daftar acara",
                "data", paginatedData
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> createEvent(@Valid @RequestBody EventRequest request) {
        EventResponse data = eventService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Acara berhasil dibuat",
                "data", data));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventRequest request) {

        EventResponse data = eventService.updateEvent(id, request);
        return ResponseEntity.ok(Map.of("message", "Acara berhasil diupdate","data", data));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.ok(Map.of("message", "Acara berhasil dihapus"));
    }
}

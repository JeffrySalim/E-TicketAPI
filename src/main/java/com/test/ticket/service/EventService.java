package com.test.ticket.service;

import com.test.ticket.dto.Event.EventRequest;
import com.test.ticket.dto.Event.EventResponse;
import com.test.ticket.dto.Pagination.PaginationResponse;
import com.test.ticket.entity.Event;
import com.test.ticket.repository.EventRepository;
import com.test.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final TicketRepository ticketRepository;

    public PaginationResponse<EventResponse> getAllEvents(int pageNo, int pageSize) {

        Pageable pageable = PageRequest.of(pageNo, pageSize);

        Page<Event> events = eventRepository.findAllByDeletedAtIsNull(pageable);

        List<EventResponse> listOfEvents = events.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PaginationResponse.<EventResponse>builder()
                .content(listOfEvents)
                .pageNo(events.getNumber())
                .pageSize(events.getSize())
                .totalElements(events.getTotalElements())
                .totalPages(events.getTotalPages())
                .isLast(events.isLast())
                .build();
    }

    public EventResponse getEventById(Long id) {
        Event event = getEventEntityById(id);
        return mapToResponse(event);
    }

    @Transactional
    public EventResponse createEvent(EventRequest request) {
        if (eventRepository.existsByTitleAndDeletedAtIsNull(request.getTitle())) {
            throw new IllegalArgumentException("Nama acara sudah digunakan");
        }

        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .eventDate(request.getEventDate())
                .price(request.getPrice())
                .quota(request.getQuota())
                .build();

        event.setCreatedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());

        Event savedEvent = eventRepository.save(event);
        return mapToResponse(savedEvent);
    }

    @Transactional
    public EventResponse updateEvent(Long id, EventRequest request) {
        Event event = getEventEntityById(id);

        if (!event.getTitle().equals(request.getTitle()) &&
                eventRepository.existsByTitleAndDeletedAtIsNull(request.getTitle())) {
            throw new IllegalArgumentException("Nama acara sudah digunakan oleh acara lain");
        }

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setEventDate(request.getEventDate());
        event.setPrice(request.getPrice());
        event.setQuota(request.getQuota());

        event.setUpdatedAt(LocalDateTime.now());

        Event updatedEvent = eventRepository.save(event);
        return mapToResponse(updatedEvent);
    }

    @Transactional
    public void deleteEvent(Long id) {
        Event event = getEventEntityById(id);

        if (ticketRepository.hasSoldTickets(id)) {
            throw new IllegalArgumentException("Tidak dapat menghapus acara karena sudah ada tiket yang terjual");
        }

        event.setDeletedAt(LocalDateTime.now());
        eventRepository.save(event);
    }

    // Helper methods
    public Event getEventEntityById(Long id) {
        return eventRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new IllegalArgumentException("Acara tidak ditemukan"));
    }

    private EventResponse mapToResponse(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .price(event.getPrice())
                .quota(event.getQuota())
                .build();
    }
}

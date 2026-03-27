package com.test.ticket.service;

import com.test.ticket.dto.Ticket.TicketRequest;
import com.test.ticket.dto.Ticket.TicketResponse;
import com.test.ticket.entity.Enum.TicketStatus;
import com.test.ticket.entity.Event;
import com.test.ticket.entity.Ticket;
import com.test.ticket.entity.User;
import com.test.ticket.repository.EventRepository;
import com.test.ticket.repository.TicketRepository;
import com.test.ticket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RedissonClient redissonClient;

    @Transactional
    public TicketResponse checkoutTicket(TicketRequest request) {
        // User yang sedang login
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalArgumentException("User tidak valid"));

        // Nama lock berdasarkan Id Event
        String lockKey = "lock:event:" + request.getEventId();
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // Coba mengunci (Tunggu 10 detik, kunci otomatis lepas setelah 5 detik jika macet)
            boolean isLocked = lock.tryLock(10, 5, TimeUnit.SECONDS);
            if (!isLocked) {
                throw new RuntimeException("Sistem sedang sibuk memproses antrean, silakan coba lagi.");
            }

            try {

                Event event = eventRepository.findByIdAndDeletedAtIsNull(request.getEventId())
                        .orElseThrow(() -> new IllegalArgumentException("Acara tidak ditemukan"));

                // Validasi kuota
                if (event.getQuota() < request.getQuantity()) {
                    throw new IllegalArgumentException("Kapasitas tidak mencukupi. Sisa kuota: " + event.getQuota());
                }

                // Kurangi kuota dan simpan
                event.setQuota(event.getQuota() - request.getQuantity());
                event.setUpdatedAt(LocalDateTime.now());
                eventRepository.save(event);

                // Hitung total harga
                BigDecimal totalPrice = event.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));

                // Generate Nomor Tiket
                String ticketNumber = generateTicketNumber(event.getTitle(), event.getEventDate());

                // Simpan Tiket
                Ticket ticket = Ticket.builder()
                        .ticketNumber(ticketNumber)
                        .user(user)
                        .event(event)
                        .quantity(request.getQuantity())
                        .totalPrice(totalPrice)
                        .status(TicketStatus.VALID)
                        .build();

                ticket.setCreatedAt(LocalDateTime.now());
                ticket.setUpdatedAt(LocalDateTime.now());
                ticketRepository.save(ticket);

                return TicketResponse.builder()
                        .ticketNumber(ticket.getTicketNumber())
                        .eventTitle(event.getTitle())
                        .quantity(ticket.getQuantity())
                        .totalPrice(ticket.getTotalPrice())
                        .status(ticket.getStatus().name())
                        .build();

            } finally {
                // Lepas kunci
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Proses terputus", e);
        }
    }

    private String generateTicketNumber(String title, LocalDateTime eventDate) {
        // 3 huruf pertama
        String prefix = title.length() >= 3 ? title.substring(0, 3).toUpperCase() : title.toUpperCase();

        // tanggal-ddMMyy
        String dateStr = eventDate.format(DateTimeFormatter.ofPattern("ddMMyy"));

        // Tambahkan 4 karakter acak (alphanumeric) agar benar-benar unik
        String randomSuffix = UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        return prefix + dateStr + "-" + randomSuffix;
    }

    public List<TicketResponse> getAllTickets() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        User currentUser = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalArgumentException("User tidak ditemukan"));

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        List<Ticket> tickets = isAdmin
                ? ticketRepository.findAllByDeletedAtIsNull()
                : ticketRepository.findAllByUserIdAndDeletedAtIsNull(currentUser.getId());

        return tickets.stream()
                .map(ticket -> TicketResponse.builder()
                        .ticketNumber(ticket.getTicketNumber())
                        .eventTitle(ticket.getEvent().getTitle())
                        .quantity(ticket.getQuantity())
                        .totalPrice(ticket.getTotalPrice())
                        .status(ticket.getStatus().name())
                        .build())
                .collect(Collectors.toList());
    }

    public TicketResponse getTicketById(Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        User currentUser = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalArgumentException("User tidak ditemukan"));

        Ticket ticket = ticketRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new IllegalArgumentException("Tiket tidak ditemukan"));

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !ticket.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Anda tidak berhak melihat tiket milik orang lain");
        }

        return TicketResponse.builder()
                .ticketNumber(ticket.getTicketNumber())
                .eventTitle(ticket.getEvent().getTitle())
                .quantity(ticket.getQuantity())
                .totalPrice(ticket.getTotalPrice())
                .status(ticket.getStatus().name())
                .build();
    }

    @Transactional
    public TicketResponse cancelTicket(Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        User actor = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalArgumentException("User tidak valid"));

        Ticket ticket = ticketRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new IllegalArgumentException("Tiket tidak ditemukan"));

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !ticket.getUser().getId().equals(actor.getId())) {
            throw new AccessDeniedException("Anda tidak berhak membatalkan tiket ini");
        }

        if (ticket.getStatus() == TicketStatus.CANCELLED) {
            throw new IllegalArgumentException("Tiket ini sudah dibatalkan sebelumnya");
        }

        Event event = ticket.getEvent();
        event.setQuota(event.getQuota() + ticket.getQuantity());
        event.setUpdatedAt(LocalDateTime.now());
        eventRepository.save(event);

        ticket.setStatus(TicketStatus.CANCELLED);
        ticket.setUpdatedAt(LocalDateTime.now());
        ticketRepository.save(ticket);

        return TicketResponse.builder()
                .ticketNumber(ticket.getTicketNumber())
                .eventTitle(event.getTitle())
                .quantity(ticket.getQuantity())
                .totalPrice(ticket.getTotalPrice())
                .status(ticket.getStatus().name())
                .build();
    }

    public Integer getTotalSoldTickets(Long eventId) {
        Integer total = ticketRepository.sumSoldTicketsByEventId(eventId);
        return total != null ? total : 0;
    }

    public Map<String, Object> getSummaryReport() {

        Integer totalUnsold = eventRepository.sumAllRemainingQuota();
        Integer totalSold = ticketRepository.countAllSoldTickets();
        BigDecimal totalRevenue = ticketRepository.sumTotalRevenue();
        Integer totalCancelled = ticketRepository.countAllCancelledTickets();
        Long totalEvents = eventRepository.countAllActiveEvents();
        Integer totalTicketsAll = totalUnsold + totalSold;

        return Map.of(
                "Total Events : ", totalEvents,
                "Total Ticket : ", totalTicketsAll,
                "Total Ticket Belum Terjual : ", totalUnsold,
                "Total Ticket Sudah Terjual : ", totalSold,
                "Total Ticket Cancel : ", totalCancelled,
                "Total Revenue : ", totalRevenue
                );
    }
}

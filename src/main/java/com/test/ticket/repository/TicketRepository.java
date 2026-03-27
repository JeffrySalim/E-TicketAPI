package com.test.ticket.repository;

import com.test.ticket.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // cek tiket terjual
    @Query("SELECT COUNT(t) > 0 FROM Ticket t WHERE t.event.id = :eventId AND t.status = 'VALID' AND t.deletedAt IS NULL")
    boolean hasSoldTickets(@Param("eventId") Long eventId);

    @Query("SELECT SUM(t.quantity) FROM Ticket t WHERE t.event.id = :eventId AND t.status = 'VALID'")
    Integer sumSoldTicketsByEventId(@Param("eventId") Long eventId);

    List<Ticket> findAllByDeletedAtIsNull();
    List<Ticket> findAllByUserIdAndDeletedAtIsNull(Long userId);

    Optional<Ticket> findByIdAndDeletedAtIsNull(Long id);

    @Query("SELECT COALESCE(SUM(t.quantity), 0) FROM Ticket t WHERE t.status = 'VALID' AND t.deletedAt IS NULL")
    Integer countAllSoldTickets();

    @Query("SELECT COALESCE(SUM(t.totalPrice), 0) FROM Ticket t WHERE t.status = 'VALID' AND t.deletedAt IS NULL")
    BigDecimal sumTotalRevenue();

    @Query("SELECT COALESCE(SUM(t.quantity), 0) FROM Ticket t WHERE t.status = 'CANCELLED' AND t.deletedAt IS NULL")
    Integer countAllCancelledTickets();
}

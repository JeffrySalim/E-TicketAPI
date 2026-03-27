package com.test.ticket.repository;

import com.test.ticket.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    Page<Event> findAllByDeletedAtIsNull(Pageable pageable);
    Optional<Event> findByIdAndDeletedAtIsNull(Long id);

    boolean existsByTitleAndDeletedAtIsNull(String title);

    @Query("SELECT COUNT(e) FROM Event e WHERE e.deletedAt IS NULL")
    Long countAllActiveEvents();

    @Query("SELECT COALESCE(SUM(e.quota), 0) FROM Event e WHERE e.deletedAt IS NULL")
    Integer sumAllRemainingQuota();
}

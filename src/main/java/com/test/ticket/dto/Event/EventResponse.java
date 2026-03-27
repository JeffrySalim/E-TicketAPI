package com.test.ticket.dto.Event;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class EventResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime eventDate;
    private BigDecimal price;
    private Integer quota;
}

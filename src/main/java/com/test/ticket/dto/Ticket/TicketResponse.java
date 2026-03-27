package com.test.ticket.dto.Ticket;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TicketResponse {
    private String ticketNumber;
    private String eventTitle;
    private Integer quantity;
    private BigDecimal totalPrice;
    private String status;
}

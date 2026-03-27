package com.test.ticket.dto.Ticket;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TicketRequest {
    @NotNull(message = "ID Event wajib diisi")
    private Long eventId;

    @NotNull(message = "Jumlah tiket wajib diisi")
    @Min(value = 1, message = "Minimal pembelian 1 tiket")
    private Integer quantity;
}

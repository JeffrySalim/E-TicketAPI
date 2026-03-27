package com.test.ticket.dto.Event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EventRequest {
    @NotBlank(message = "Nama acara wajib diisi")
    private String title;

    private String description;

    @NotNull(message = "Tanggal acara wajib diisi")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    @NotNull(message = "Harga wajib diisi")
    @Min(value = 0, message = "Harga tidak boleh negatif")
    private BigDecimal price;

    @NotNull(message = "Kuota wajib diisi")
    @Min(value = 1, message = "Kuota minimal 1")
    private Integer quota;
}

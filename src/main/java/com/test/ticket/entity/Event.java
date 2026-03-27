package com.test.ticket.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "events")
public class Event extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150, unique = true)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @Min(value = 0, message = "Harga tidak boleh negatif")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Min(value = 0, message = "Kapasitas tidak boleh negatif")
    @Column(nullable = false)
    private Integer quota;
}

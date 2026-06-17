package org.tradebook.journal.features.journal.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "instruments", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "symbol", "exchange", "type" })
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Instrument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String symbol;

    @Column(nullable = false, length = 20)
    private String exchange;

    @Column(nullable = false, length = 20)
    private String type; // EQUITY, OPTION, FUTURE
}

package org.tradebook.journal.features.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.tradebook.journal.common.entity.BaseEntity;
import org.tradebook.journal.features.auth.enums.Role;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(length = 3, nullable = false)
    private String currency; // USD, INR, etc.

    @Column(name = "mobile_number", length = 15)
    private String mobileNumber;

    @Column(name = "residential_address", length = 255)
    private String residentialAddress;

    @Column(name = "pan", length = 10)
    private String pan;

    @Column(name = "trading_style", length = 30)
    private String tradingStyle;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.USER;
}


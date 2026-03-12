package com.xpro.rentalmain.rentalmain.entity;

import com.xpro.rentalmain.rentalmain.model.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@Entity
@Table(name = "privileges")
@AllArgsConstructor
@NoArgsConstructor
public class Privilege extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String name; // e.g., "CREATE_LISTING"

    private String scope; // e.g., "STORAGE_SERVICE" or "USER_MANAGEMENT"

    private String description; // e.g., "Allows the creation of new storage unit entries"

}
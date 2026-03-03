package com.ticketing.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity(name = "stores")
@Getter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class Store {
    @Id
    @Column(name = "store_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Double latitude;
    private Double longitude;
}

package com.ticketing.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Entity(name = "stores")
@Getter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class Store {
    @Id
    @Column(name = "store_id")
    private Long id;
    private String name;
    private Double latitude;
    private Double longitude;
}

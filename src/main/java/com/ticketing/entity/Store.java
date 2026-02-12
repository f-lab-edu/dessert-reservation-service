package com.ticketing.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Entity(name = "stores")
@Getter
@Builder
@RequiredArgsConstructor
public class Store {
    @Id
    @Column(name = "store_id")
    private Long id;
    private String name;
    private Float latitude;
    private Float longitude;
}

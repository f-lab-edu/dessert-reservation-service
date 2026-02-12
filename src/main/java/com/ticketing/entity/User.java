package com.ticketing.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity(name = "users")
@Getter
@Builder
@RequiredArgsConstructor
public class User {
    @Id
    @Column(name = "user_id")
    private Long id;
    private String name;
    private String email;
    private String password;
    @Column(name = "push_token")
    private String pushToken;
    @Column(name = "created_dt")
    @CreatedDate
    private LocalDateTime createdDt;
    @Column(name = "deleted_dt")
    private LocalDateTime deletedDt;
}

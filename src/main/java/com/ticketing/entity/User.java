package com.ticketing.entity;

import com.ticketing.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity(name = "users")
@Getter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {
    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String password;
    @Column(name = "push_token")
    private String pushToken;
    @Column(name = "deleted_dt")
    private LocalDateTime deletedDt;
}

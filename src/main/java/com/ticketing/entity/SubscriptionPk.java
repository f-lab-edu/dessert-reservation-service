package com.ticketing.entity;


import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class SubscriptionPk implements Serializable {
    private Long userId;
    private Long storeId;
}

package com.ticketing.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DessertTest {
    @Test
    @DisplayName("정상 범위 에러없음")
    void decreaseInventorySuccessfully(){
        Dessert dessert = Dessert.builder().inventory(3).build();
        dessert.decreaseInventory(1);
        assertEquals(2, dessert.getInventory());
    }

    @Test
    @DisplayName("count가 inventory보다 크면 에러")
    void throwsExceptionWhenCountExceedsInventory(){
        Dessert dessert = Dessert.builder().inventory(2).build();
        assertThrows(IllegalArgumentException.class, () -> dessert.decreaseInventory(3));
        assertEquals(2, dessert.getInventory());
    }

    @Test
    @DisplayName("count가 inventory와 같으면 재고가 0이 된다")
    void inventoryBecomesZeroWhenCountEqualsInventory() {
        Dessert dessert = Dessert.builder().inventory(2).build();
        dessert.decreaseInventory(2);
        assertEquals(0, dessert.getInventory());
    }
}

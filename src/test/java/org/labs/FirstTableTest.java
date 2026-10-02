package org.labs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TableTest {

    @Test
    void rejectsLessThanTwoProgrammers() {
        assertThrows(IllegalArgumentException.class, () -> new CorrectTable(1, 1, 1));
    }

    @Test
    void rejectsZeroWaiters() {
        assertThrows(IllegalArgumentException.class, () -> new CorrectTable(4, 0, 1));
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void everyDishIsEatenExactlyOnceInTotal() throws InterruptedException {
        int dishes = 500;
        Table firstTable = new CorrectTable(5, 2, dishes);

        firstTable.startEating();

        int total = firstTable.getInfo().stream().mapToInt(IProgrammer::getEaten).sum();
        assertEquals(dishes, total);
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void dishesAreDistributedFairly() throws InterruptedException {
        Table firstTable = new CorrectTable(7, 3, 700);

        firstTable.startEating();

        List<IProgrammer> info = firstTable.getInfo();
        int max = info.stream().mapToInt(IProgrammer::getEaten).max().orElseThrow();
        int min = info.stream().mapToInt(IProgrammer::getEaten).min().orElseThrow();

        assertTrue(max - min <= 1, "разброс поеденных блюд между программистами: " + (max - min));
    }
}

package org.labs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.labs.domain.Programmer;
import org.labs.domain.Table;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirstTableTest {

    @Test
    void rejectsLessThanTwoProgrammers() {
        assertThrows(IllegalArgumentException.class, () -> new Table(1, 1, 1));
    }

    @Test
    void rejectsZeroWaiters() {
        assertThrows(IllegalArgumentException.class, () -> new Table(4, 0, 1));
    }

    @Test
    void rejectsNegativeDishes() {
        assertThrows(IllegalArgumentException.class, () -> new Table(4, 1, -1));
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void finishesWhenThereAreNoDishes() {
        Table table = new Table(5, 2, 0);

        table.startEating();

        assertEquals(0, totalEaten(table));
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void everyDishIsEatenExactlyOnceInTotal() {
        int dishes = 10_000;
        Table table = new Table(5, 2, dishes);

        table.startEating();

        assertEquals(dishes, totalEaten(table));
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void worksWithMoreWaitersThanProgrammers() {
        Table table = new Table(2, 5, 1_000);

        table.startEating();

        assertEquals(1_000, totalEaten(table));
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void dishesAreDistributedFairly() {
        Table table = new Table(7, 3, 100_000);

        table.startEating();

        List<Programmer> programmers = table.getProgrammers();
        int max = programmers.stream().mapToInt(Programmer::getEaten).max().orElseThrow();
        int min = programmers.stream().mapToInt(Programmer::getEaten).min().orElseThrow();

        assertTrue(max - min <= 1, "разброс поеденных блюд между программистами: " + (max - min));
    }

    private static int totalEaten(Table table) {
        return table.getProgrammers().stream().mapToInt(Programmer::getEaten).sum();
    }
}

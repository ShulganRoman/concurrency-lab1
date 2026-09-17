package org.labs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TableTest {

    @Test
    void rejectsLessThanTwoProgrammers() {
        assertThrows(IllegalArgumentException.class, () -> new Table(1, 1, 1));
    }

    @Test
    void rejectsZeroWaiters() {
        assertThrows(IllegalArgumentException.class, () -> new Table(4, 0, 1));
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void everyDishIsEatenExactlyOnceInTotal() {
        int dishes = 500;
        Table table = new Table(5, 2, dishes);

        table.run();

        int total = table.getInfo().stream().mapToInt(p -> p.getEaten().get()).sum();
        assertEquals(dishes, total);
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void dishesAreDistributedFairly() {
        Table table = new Table(7, 3, 700);

        table.run();

        List<Programmer> info = table.getInfo();
        int max = info.stream().mapToInt(p -> p.getEaten().get()).max().orElseThrow();
        int min = info.stream().mapToInt(p -> p.getEaten().get()).min().orElseThrow();

        assertTrue(max - min <= 1, "разброс поеденных блюд между программистами: " + (max - min));
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void neighboursNeverEatAtTheSameTime() {
        int programmers = 6;

        Map<Integer, Boolean> eating = new ConcurrentHashMap<>();
        AtomicBoolean violation = new AtomicBoolean(false);

        Runnable job = () -> {
            int index = Integer.parseInt(Thread.currentThread().getName());
            int left = (index - 1 + programmers) % programmers;
            int right = (index + 1) % programmers;

            if (Boolean.TRUE.equals(eating.get(left)) || Boolean.TRUE.equals(eating.get(right))) {
                violation.set(true);
            }
            eating.put(index, true);

            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            eating.put(index, false);
        };

        Table table = new Table(programmers, 3, 150, job);
        table.run();

        assertFalse(violation.get(), "два соседних программиста одновременно держали общую ложку");
    }
}

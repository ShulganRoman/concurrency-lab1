package org.labs.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ProgrammerTest {
    private Spoon left;
    private Spoon right;
    private Programmer programmer;
    private Thread thread;

    @BeforeEach
    void setUp() {
        left = new Spoon();
        right = new Spoon();
        programmer = new Programmer(left, right);
        thread = new Thread(programmer);
        thread.start();
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void eatsOnceAndReturnsSpoonsWhenSignaledToEat() throws InterruptedException {
        programmer.startEating();

        // stop() берёт тот же Lock, что держит run() на протяжении всего eat(),
        // поэтому гарантированно дождётся завершения текущего приёма пищи
        programmer.stop();
        thread.join();

        assertEquals(1, programmer.getEaten());
        assertFalse(left.getInUse());
        assertFalse(right.getInUse());
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void stopTerminatesIdleProgrammerWithoutEating() throws InterruptedException {
        programmer.stop();
        thread.join();

        assertFalse(thread.isAlive());
        assertEquals(0, programmer.getEaten());
    }
}

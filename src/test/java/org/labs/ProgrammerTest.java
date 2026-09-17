package org.labs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ProgrammerTest {

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void eatRunsTheJobExactlyOnceAndIncrementsEaten() throws InterruptedException {
        AtomicInteger jobRuns = new AtomicInteger(0);
        Programmer programmer = new Programmer(jobRuns::incrementAndGet);
        Thread thread = new Thread(programmer);
        thread.start();

        programmer.eat();
        programmer.awaitDone();

        assertEquals(1, jobRuns.get());
        assertEquals(1, programmer.getEaten().get());

        programmer.stop();
        thread.join();
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void awaitDoneDoesNotReturnBeforeTheJobActuallyFinishes() throws InterruptedException {
        AtomicInteger stage = new AtomicInteger(0);
        Programmer programmer = new Programmer(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            stage.set(1);
        });
        Thread thread = new Thread(programmer);
        thread.start();

        programmer.eat();
        programmer.awaitDone();

        assertEquals(1, stage.get(), "awaitDone() вернулась раньше, чем job() реально завершилась");

        programmer.stop();
        thread.join();
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void stopTerminatesTheRunLoop() throws InterruptedException {
        Programmer programmer = new Programmer(() -> {});
        Thread thread = new Thread(programmer);
        thread.start();

        programmer.stop();
        thread.join();

        assertFalse(thread.isAlive());
    }
}

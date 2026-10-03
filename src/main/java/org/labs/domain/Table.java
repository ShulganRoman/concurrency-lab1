package org.labs.domain;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

public class Table {
    private static final int MAX_LEAD = 0;
    private static final long TERMINATION_TIMEOUT_MINUTES = 60;

    private final int waiterAmount;
    private final AtomicInteger dishes;
    private final List<Programmer> programmers;
    private final Lock lock = new ReentrantLock();
    private final Condition spoonsReleased = lock.newCondition();

    public Table(int programmerAmount, int waiterAmount, int dishAmount) {
        if (programmerAmount < 2)
            throw new IllegalArgumentException("at least 2 programmers required, got " + programmerAmount);
        if (waiterAmount < 1)
            throw new IllegalArgumentException("at least 1 waiter required, got " + waiterAmount);
        if (dishAmount < 0)
            throw new IllegalArgumentException("dish amount must not be negative, got " + dishAmount);

        this.waiterAmount = waiterAmount;
        this.dishes = new AtomicInteger(dishAmount);

        List<Spoon> spoons = IntStream
                .range(0, programmerAmount)
                .mapToObj(_ -> new Spoon())
                .toList();

        this.programmers = IntStream.range(0, programmerAmount)
                .mapToObj(i -> new Programmer(
                        spoons.get(i),
                        spoons.get((i + 1) % programmerAmount),
                        lock,
                        spoonsReleased))
                .toList();
    }

    public void startEating() {
        ExecutorService programmerThreadPool = Executors.newFixedThreadPool(programmers.size());
        ExecutorService waiterThreadPool = Executors.newFixedThreadPool(waiterAmount);

        try {
            for (var programmer : programmers)
                programmerThreadPool.submit(programmer);

            for (int i = 0; i < waiterAmount; i++) {
                waiterThreadPool.submit(() -> {
                    try {
                        while (dishes.getAndUpdate(d -> d > 0 ? d - 1 : 0) > 0)
                            serveHungriest();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }

            waiterThreadPool.shutdown();
            if (!waiterThreadPool.awaitTermination(TERMINATION_TIMEOUT_MINUTES, TimeUnit.MINUTES))
                throw new IllegalStateException("waiters did not finish within " + TERMINATION_TIMEOUT_MINUTES + " minutes");

            for (Programmer programmer : programmers)
                programmer.stop();

            programmerThreadPool.shutdown();
            if (!programmerThreadPool.awaitTermination(TERMINATION_TIMEOUT_MINUTES, TimeUnit.MINUTES))
                throw new IllegalStateException("programmers did not finish eating within " + TERMINATION_TIMEOUT_MINUTES + " minutes");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            waiterThreadPool.shutdownNow();
            programmerThreadPool.shutdownNow();
        }
    }

    public List<Programmer> getProgrammers() {
        return programmers;
    }

    private void serveHungriest() throws InterruptedException {
        lock.lock();

        try {
            Programmer hungriest;
            while ((hungriest = findHungriest()) == null)
                spoonsReleased.await();

            hungriest.giveSpoons();
        } finally {
            lock.unlock();
        }
    }

    private Programmer findHungriest() {
        int minServed = Integer.MAX_VALUE;
        for (Programmer programmer : programmers)
            minServed = Math.min(minServed, programmer.getServed());

        Programmer hungriest = null;
        for (Programmer programmer : programmers) {
            if (!programmer.canEat() || programmer.getServed() > minServed + MAX_LEAD)
                continue;

            if (hungriest == null || programmer.getServed() < hungriest.getServed())
                hungriest = programmer;
        }

        return hungriest;
    }
}

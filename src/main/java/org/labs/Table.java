package org.labs;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

public class Table {
    private final AtomicInteger dishes;

    private final List<Spoon> spoons;
    private final List<Programmer> programmerList;

    private final ExecutorService tp_p;
    private final ExecutorService tp_w;

    private final Lock lock = new ReentrantLock();

    private final int waiters;

    public Table(int programmers, int waiters, int dishes) throws IllegalArgumentException {
        if (programmers < 2 || waiters < 1 || dishes < 0)
            throw new IllegalArgumentException();

        this.waiters = Math.min(waiters, programmers);

        this.dishes = new AtomicInteger(dishes);

        this.spoons = IntStream.range(0, programmers)
                .mapToObj(_ -> new Spoon())
                .toList();

        this.programmerList = IntStream.range(0, programmers)
                .mapToObj(_ -> new Programmer())
                .toList();

        tp_w = Executors.newFixedThreadPool(this.waiters);
        tp_p = Executors.newFixedThreadPool(programmers);
    }

    public void startEating() {
        for (int i = 0; i < waiters; i++) {
            tp_w.execute(() -> {
                int n = programmerList.size();
                int j = 0;

                while (true) {
                    if (dishes.getAndUpdate(d -> d > 0 ? d - 1 : 0) == 0) return;

                    while (true) {
                        int idx = j;
                        Spoon left = spoons.get(idx);
                        Spoon right = spoons.get((idx + 1) % n);
                        j = (j + 1) % n;

                        if (tryTake(left, right)) {
                            Programmer p = programmerList.get(idx);

                            tp_p.execute(() -> {
                                try {
                                    p.run();
                                } finally {
                                    release(left, right);
                                }
                            });

                            break;
                        }

                        Thread.onSpinWait();
                    }
                }
            });
        }

        try {
            tp_w.shutdown();
            var _ = tp_w.awaitTermination(1, TimeUnit.HOURS);
            tp_p.shutdown();
            var _ = tp_p.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            tp_w.shutdownNow();
            tp_p.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public List<Programmer> getInfo() {
        return programmerList;
    }

    private boolean tryTake(Spoon left, Spoon right) {
        lock.lock();

        try {
            if (left.isOnTable() && right.isOnTable()) {
                left.pickMe();
                right.pickMe();

                return true;
            }

            return false;
        } finally {
            lock.unlock();
        }
    }

    private void release(Spoon left, Spoon right) {
        lock.lock();

        try {
            left.putMeBack();
            right.putMeBack();
        } finally {
            lock.unlock();
        }
    }
}

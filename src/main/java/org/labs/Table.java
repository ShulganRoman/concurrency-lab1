package org.labs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Table {
    private final int dishes;
    private final int programmers;
    private final List<Spoon> spoons;
    private final Lock lock = new ReentrantLock();
    private final Condition spoonsAvailable = lock.newCondition();
    private final List<Programmer> programmerList;
    private final List<Thread> programmerThreads;
    private final ThreadPoolExecutor waiterPoolExecutor;

    public Table(int programmers, int waiters, int dishes, Runnable r) throws IllegalArgumentException {
        this.dishes = dishes;
        this.programmers = programmers;

        if (programmers < 2 || waiters < 1)
            throw new IllegalArgumentException();

        if (programmers < waiters) waiters = programmers;

        waiterPoolExecutor = new ThreadPoolExecutor(
                waiters, waiters,
                100, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(dishes));

        spoons = Stream
                .generate(Spoon::new)
                .limit(programmers)
                .collect(Collectors.toCollection(ArrayList::new));

        programmerList = IntStream
                .range(0, programmers)
                .mapToObj(i -> new Programmer(r))
                .limit(programmers)
                .toList();

        programmerThreads = IntStream
                .range(0, programmers)
                .mapToObj(i -> new Thread(programmerList.get(i), "" + i))
                .toList();
    }

    public Table(int programmers, int waiters, int dishes) {
        this(programmers, waiters, dishes, () -> {});
    }

    public void run() {
        programmerThreads.forEach(Thread::start);

        for (int i = 0; i < dishes; i++) {
            waiterPoolExecutor.execute(() -> {
                lock.lock();
                int n = getStarve();
                Programmer p = programmerList.get(n);
                p.setHasWaiter(true);
                lock.unlock();

                Spoon leftSpoon = spoons.get(n);
                Spoon rightSpoon = spoons.get((n + 1) % programmers);

                lock.lock();
                try {
                    while (!(leftSpoon.isOnTable() && rightSpoon.isOnTable())) {
                        try {
                            spoonsAvailable.await();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            p.setHasWaiter(false);
                            return;
                        }
                    }
                    leftSpoon.pickMe();
                    rightSpoon.pickMe();
                } finally {
                    lock.unlock();
                }

                p.eat();
                try {
                    p.awaitDone();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                lock.lock();
                try {
                    leftSpoon.putMeBack();
                    rightSpoon.putMeBack();
                    spoonsAvailable.signalAll();
                } finally {
                    lock.unlock();
                }

                p.setHasWaiter(false);
            });
        }

        waiterPoolExecutor.shutdown();
        try {
            waiterPoolExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        for (var p : programmerList) p.stop();

        for (var t : programmerThreads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public List<Programmer> getInfo() {
        return programmerList;
    }

    private int getStarve() {
        int result = 0;
        int min = Integer.MAX_VALUE;

        for (int i = 0; i < programmerList.size(); i++) {
            if (programmerList.get(i).getHasWaiter().get()) continue;

            int temp = programmerList.get(i).getEaten().get();
            if (temp < min) {
                min = temp;
                result = i;
            }
        }

        return result;
    }
}

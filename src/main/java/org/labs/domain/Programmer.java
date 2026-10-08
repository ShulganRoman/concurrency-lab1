package org.labs.domain;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class Programmer implements Runnable {
    private int eaten = 0;
    private final Spoon leftSpoon, rightSpoon;
    private final Semaphore eatRequests = new Semaphore(0);
    private final AtomicInteger pickCount = new AtomicInteger(0);

    private volatile boolean stopped = false;

    Programmer(Spoon leftSpoon, Spoon rightSpoon) {
        this.leftSpoon = leftSpoon;
        this.rightSpoon = rightSpoon;
    }

    public void stop() {
        stopped = true;
        eatRequests.release();
    }

    public int getPickCount() {
        return pickCount.get();
    }

    public Programmer pick() {
        pickCount.incrementAndGet();
        return this;
    }

    public int getEaten() {
        return eaten;
    }

    public void startEating() {
        eatRequests.release();
    }

    @Override
    public void run() {
        while (true) {
            try {
                eatRequests.acquire();
                if (stopped && eatRequests.availablePermits() == 0) return;

                eat();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void eat() throws InterruptedException {
        Spoon first = leftSpoon.getId() < rightSpoon.getId() ? leftSpoon : rightSpoon;
        Spoon second = first == leftSpoon ? rightSpoon : leftSpoon;

        first.getLock().lock();
        second.getLock().lock();

        boolean tookFirst = false;
        boolean tookSecond = false;
        try {
            while (!(tookFirst = first.tryTakeSpoon()))
                first.getCondition().await();

            while (!(tookSecond = second.tryTakeSpoon()))
                second.getCondition().await();

            eaten++;
        } finally {
            if (tookFirst) releaseSpoon(first);
            if (tookSecond) releaseSpoon(second);

            first.getLock().unlock();
            second.getLock().unlock();
        }
    }

    private void releaseSpoon(Spoon spoon) {
        spoon.putSpoon();
        spoon.getCondition().signal();
    }
}

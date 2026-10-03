package org.labs.domain;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class Programmer implements Runnable {
    private final AtomicInteger eaten = new AtomicInteger();
    private final Spoon leftSpoon, rightSpoon;
    private final Lock lock;
    private final Condition condition;
    private final Condition spoonsReleased;

    private boolean hasSpoons = false;
    private boolean stopped = false;
    private int served = 0;

    Programmer(Spoon leftSpoon, Spoon rightSpoon, Lock lock, Condition spoonsReleased) {
        this.leftSpoon = leftSpoon;
        this.rightSpoon = rightSpoon;
        this.lock = lock;
        this.spoonsReleased = spoonsReleased;

        this.condition = lock.newCondition();
    }

    public int getEaten() {
        return eaten.get();
    }

    @Override
    public void run() {
        while (true) {
            lock.lock();

            try {
                while (!hasSpoons && !stopped)
                    condition.await();

                if (!hasSpoons) return;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } finally {
                lock.unlock();
            }

            eaten.incrementAndGet();

            releaseSpoons();
        }
    }

    public void stop() {
        lock.lock();

        try {
            stopped = true;
            condition.signal();
        } finally {
            lock.unlock();
        }
    }

    int getServed() {
        return served;
    }

    boolean canEat() {
        return !hasSpoons && leftSpoon.isOnTable() && rightSpoon.isOnTable();
    }

    void giveSpoons() {
        leftSpoon.takeSpoon();
        rightSpoon.takeSpoon();
        hasSpoons = true;
        served++;
        condition.signal();
    }

    private void releaseSpoons() {
        lock.lock();

        try {
            leftSpoon.putSpoon();
            rightSpoon.putSpoon();
            hasSpoons = false;
            spoonsReleased.signalAll();
        } finally {
            lock.unlock();
        }
    }
}

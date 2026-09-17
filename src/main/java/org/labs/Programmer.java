package org.labs;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Programmer implements Runnable {

    private final Semaphore semaphore = new Semaphore(0);
    private final Semaphore doneSemaphore = new Semaphore(0);
    private volatile boolean runnable = true;
    private final Runnable r;
    private final AtomicInteger eaten;
    private final AtomicBoolean hasWaiter;

    public Programmer(Runnable r) {
        this(r, new AtomicInteger(0), new AtomicBoolean(false));
    }

    public Programmer(Runnable r, AtomicInteger eaten, AtomicBoolean hasWaiter) {
        this.r = r;
        this.eaten = eaten;
        this.hasWaiter = hasWaiter;
    }

    public AtomicInteger getEaten() {
        return eaten;
    }

    public void setEaten(int value) {
        eaten.set(value);
    }

    public AtomicBoolean getHasWaiter() {
        return hasWaiter;
    }

    public void setHasWaiter(boolean value) {
        hasWaiter.set(value);
    }

    @Override
    public void run() {
        while (runnable) {
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println(e.getMessage());
            }
            if (!runnable) break;

            job();
        }
    }

    public void eat() {
        semaphore.release();
    }

    public void awaitDone() throws InterruptedException {
        doneSemaphore.acquire();
    }

    public void stop() {
        runnable = false;
        semaphore.release();
    }

    private void job() {
        r.run();
        eaten.getAndIncrement();
        doneSemaphore.release();
    }
}
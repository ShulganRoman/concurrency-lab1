package org.labs;

import java.util.concurrent.atomic.AtomicInteger;

public class Programmer {
    private final AtomicInteger counter = new AtomicInteger(0);

    public void run() {
        counter.incrementAndGet();
    }

    public int getEaten() {
        return counter.get();
    }
}
package org.labs.domain;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Spoon {
    private static final AtomicInteger NEXT_ID = new AtomicInteger(0);

    private final int id = NEXT_ID.getAndIncrement();
    private final AtomicBoolean inUse = new AtomicBoolean(false);
    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

    public int getId() {
        return id;
    }

    public boolean getInUse() {
        return inUse.get();
    }

    public boolean tryTakeSpoon() {
        return !inUse.getAndSet(true);
    }

    public void putSpoon() {
        if (!inUse.getAndSet(false))
            throw new IllegalStateOfSpoonException();
    }

    public Condition getCondition() {
        return condition;
    }

    public Lock getLock() {
        return lock;
    }
}

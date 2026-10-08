package org.labs.domain;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;

public class Waiter implements Runnable {
    private final AtomicInteger dishes;
    private final List<Programmer> programmers;
    private final Lock selectionLock;

    Waiter(AtomicInteger dishes, List<Programmer> programmers, Lock selectionLock) {
        this.dishes = dishes;
        this.programmers = programmers;
        this.selectionLock = selectionLock;
    }

    @Override
    public void run() {
        while (dishes.getAndUpdate(d -> d > 0 ? d - 1 : 0) > 0)
            serveHungriest();
    }

    private void serveHungriest() {
        Programmer hungriest = findHungriest();
        hungriest.startEating();
    }

    private Programmer findHungriest() {
        selectionLock.lock();
        try {
            Programmer hungriest = null;
            int min = Integer.MAX_VALUE;

            for (Programmer programmer : programmers) {
                int pickCount = programmer.getPickCount();

                if (pickCount < min) {
                    min = pickCount;
                    hungriest = programmer;
                }
            }

            assert hungriest != null;
            return hungriest.pick();
        } finally {
            selectionLock.unlock();
        }
    }
}

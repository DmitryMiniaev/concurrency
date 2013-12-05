package hw1;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Semaphore {
    private final int CAPACITY;
    private Lock lock = new ReentrantLock();
    private Condition notFull = lock.newCondition();
    private int cnt;

    public Semaphore(int capacity) {
        this.CAPACITY = capacity;
    }

    public void acquire() throws InterruptedException {
        lock.lock();
        try {
            while (cnt + 1 == CAPACITY) {
                notFull.await();
            }
            cnt++;
        } finally {
            lock.unlock();
        }
    }

    public void release() {
        lock.lock();
        try {
            cnt = (cnt > 0) ? cnt - 1 : cnt;
            notFull.signalAll();
        }finally {
            lock.unlock();
        }
    }
}

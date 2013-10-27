package hw1;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Dimon
 * Date: 8/14/13
 * Time: 11:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class Semaphore {
    private int maxThreads;
    private int curThreads;
    private Queue<Long> waitingThreads = new LinkedList<Long>();

    public Semaphore(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    public synchronized void acquire() throws InterruptedException {
        curThreads++;
        waitingThreads.add(Thread.currentThread().getId());
        while (curThreads > maxThreads) {
            this.wait();
            boolean isLockedForThisThread = !waitingThreads.isEmpty()
                    && waitingThreads.peek() != Thread.currentThread().getId();
            if (!isLockedForThisThread) {
                this.notify();
                this.wait();
            }
            waitingThreads.remove();
        }
    }

    public synchronized void release() {
        if (!waitingThreads.isEmpty()) {
            curThreads--;
            this.notify();
        }
    }
}

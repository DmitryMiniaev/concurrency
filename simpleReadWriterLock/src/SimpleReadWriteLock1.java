import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SimpleReadWriteLock1 {
    private int readers;
    private boolean writer;
    private Lock lock;
    private Condition condition;
    private MyLock readLock;
    private MyLock writeLock;

    public SimpleReadWriteLock1() {
        writer = false;
        readers = 0;
        lock = new ReentrantLock();
        readLock = new MyReadLock();
        writeLock = new MyWriteLock();
        condition = lock.newCondition();
    }

    public MyLock readLock() {
        return readLock;
    }

    public MyLock writeLock() {
        return writeLock;
    }

    protected interface MyLock {
        void lock() throws InterruptedException;
        void unlock();
    }

    protected class MyReadLock implements MyLock{

        @Override
        public void lock() throws InterruptedException {
            lock.lock();
            try {
                while (writer) {
                    condition.await();
                }
                readers++;
            }finally {
                lock.unlock();
            }
        }

        @Override
        public void unlock() {
            lock.lock();
            try {
                readers--;
                if(readers == 0) {
                    condition.signalAll();
                }
            } finally {
                lock.unlock();
            }
        }
    }

    protected class MyWriteLock implements MyLock {

        @Override
        public void lock() throws InterruptedException {
           lock.lock();
           try {
             while (readers > 0 || writer) {
                 condition.await();
             }
             writer = true;
           } finally {
             lock.unlock();
           }
        }

        @Override
        public void unlock() {
            lock.lock();
            try {
               writer = false;
               condition.signalAll();
            } finally {
                lock.unlock();
            }
        }
    }
}

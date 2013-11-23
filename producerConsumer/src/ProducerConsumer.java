import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import static java.lang.System.out;

public class ProducerConsumer {

    private volatile boolean isStopped;
    private ConditionQueue<Integer> q;

    public static void main(String[] args) throws InterruptedException {
        ProducerConsumer pc = new ProducerConsumer();
        pc.simulate();
    }

    public void simulate() throws InterruptedException {
        out.println("[Start]");
        Producer p = new Producer();
        Consumer c = new Consumer();
        q = new ConditionQueue<Integer>(10);
        p.start();
        c.start();
        p.join();
        c.join();
        out.println("[End]");
    }

    protected class Producer extends Thread {

        @Override
        public void run() {
            for (int i = 0; i < 100; i++) {
                try {
                    q.put(i);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected class Consumer extends Thread {
        @Override
        public void run() {
            for (int i = 0; i < 100; i++) {
                try {
                    Integer elem = q.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(150);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected abstract class AbstractBoundedQueue<T> {

        protected final int SIZE;
        protected T[] t;
        protected int head;
        protected int tail;
        protected int cnt;

        public AbstractBoundedQueue(int size) {
            SIZE = size;
            t = (T[]) new Object[size];
        }

        abstract public void put(T elem) throws InterruptedException;

        abstract public T take() throws InterruptedException;
    }

    protected class MonitorQueue<T> extends AbstractBoundedQueue<T> {

        public MonitorQueue(int size) {
            super(size);
        }

        @Override
        public synchronized void put(T elem) throws InterruptedException {
            out.println("[Attempt to put " + elem + "]");
            while (cnt == SIZE) {
                out.println("[Queue is full]");
                wait();
            }
            out.println("[Putting " + elem + "]");
            t[tail] = elem;
            tail = (tail + 1) % SIZE;
            ++cnt;
            notifyAll();
        }

        @Override
        public synchronized T take() throws InterruptedException {
            T result = null;
            out.println("[Attempt to take " + t[head] + "]");
            while (cnt == 0) {
                out.println("Queue is empty");
                wait();
            }
            out.println("[Taking " + t[head] +"]");
            result = t[head];
            head = (head + 1) % SIZE;
            --cnt;
            return result;
        }
    }

    protected class ConditionQueue<T> extends AbstractBoundedQueue<T> {

        private Lock lock = new ReentrantLock();
        private Condition notFull = lock.newCondition();
        private Condition notEmpty = lock.newCondition();

        public ConditionQueue(int size) {
            super(size);
        }

        public void put(T elem) throws InterruptedException {
            lock.lock();
            try {
                out.println("[Attempt to put " + elem + "]");
                while (cnt == SIZE) {
                    out.println("[Queue is full]");
                    notFull.await();
                }
                out.println("[Putting " + elem + "]");
                t[tail] = elem;
                tail = (tail + 1) % SIZE;
                ++cnt;
                notEmpty.signalAll();
            } finally {
                lock.unlock();
            }
        }

        public T take() throws InterruptedException {
            T result = null;
            lock.lock();
            try {
                out.println("[Attempt to take " + t[head] + "]");
                while (cnt == 0) {
                    out.println("Queue is empty");
                    notEmpty.await();
                }
                out.println("[Taking " + t[head] +"]");
                result = t[head];
                head = (head + 1) % SIZE;
                --cnt;
                notFull.signalAll();
            } finally {
                lock.unlock();
            }
            return result;
        }
    }
}

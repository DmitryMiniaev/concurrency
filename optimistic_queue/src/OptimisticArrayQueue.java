import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.atomic.AtomicStampedReference;

public class OptimisticArrayQueue<T> {

    private AtomicReferenceArray<T> a;
    private AtomicInteger head = new AtomicInteger(0);
    private AtomicInteger tail = new AtomicInteger(0);
    private final int CAPACITY;
    volatile int size = 0;
    private final int reserve = -1;
    private final int reserve2 = -2;

    public OptimisticArrayQueue(int capacity) {
        CAPACITY = capacity;
        a = new AtomicReferenceArray<T>(CAPACITY);
    }


    public void enq(T value) {
        y:
        for (; ; ) {
            x:
            for(;;)
            if(size != reserve) {
                int newSize = size + 1;
                if (newSize != CAPACITY) {
                    Integer oldTail = tail.get();
                    Integer newTail = (oldTail + 1) % CAPACITY;
                    size = reserve;
                    if (tail.compareAndSet(oldTail, newTail)) {
                        a.set(oldTail, value);
                        size = newSize;
                        break y;
                    }
                }
            }
        }
    }

    public T deq() {
        T result;
        for (;;) {
            if(size != reserve2) {
                int newSize = size - 1;
                if(newSize != 0) {
                    Integer oldHead = head.get();
                    Integer newHead = (oldHead + 1) % CAPACITY;
                    size = reserve2;
                    if(head.compareAndSet(oldHead, newHead)) {
                        result = a.get(oldHead);
                        size = newSize;
                        return result;
                    }
                }
            }
        }
    }
}

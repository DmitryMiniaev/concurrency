import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class OptimisticArrayQueue2<T> {


    private AtomicReferenceArray<T> a;
    private AtomicInteger size = new AtomicInteger(0);
    private AtomicInteger head = new AtomicInteger(0);
    private AtomicInteger tail = new AtomicInteger(0);
    private final int CAPACITY;

    public OptimisticArrayQueue2(int capacity) {
        CAPACITY = capacity;
        a = new AtomicReferenceArray<T>(CAPACITY);
    }

    public void enq(T value) {
        for (;;) {
            int oldSize = size.get();
            int newSize = oldSize + 1;
            if(newSize < CAPACITY) {
                if(size.compareAndSet(oldSize, newSize)) {
                    break;
                }
            }
        }
        for(;;) {
            int oldTail = tail.get();
            int newTail = (oldTail + 1) % CAPACITY;
            if(tail.compareAndSet(oldTail, newTail)){
                a.set(oldTail, value);
                return;
            }
        }
    }

    public T deq() {
        T result;
        for (;;) {
            int oldSize = size.get();
            int newSize = oldSize - 1;
            if(newSize >= 0) {
                if(size.compareAndSet(oldSize, newSize)) {
                   break;
                }
            }
            if(newSize == -1) {
                break;
            }
        }
        for(;;) {
            int oldHead = head.get();
            int newHead = (oldHead + 1) % CAPACITY;
            result = a.get(oldHead);
            if(head.compareAndSet(oldHead, newHead)){
                return result;
            }
        }
    }
}

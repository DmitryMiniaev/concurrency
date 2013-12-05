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
        s:
        for (;;) {
            int oldSize = size.get();
            int newSize = oldSize + 1;
            if(newSize != CAPACITY) {
                if(size.compareAndSet(oldSize, newSize)) {
                    for(;;) {
                        int oldTail = tail.get();
                        int newTail = oldTail + 1;
                        if(tail.compareAndSet(oldTail, newTail)){
                            a.set(oldTail, value);
                            break s;
                        }
                    }
                }
            }
        }
    }

    public T deq() {
        T result;
        for (;;) {
            int oldSize = size.get();
            int newSize = oldSize - 1;
            if(newSize != 0) {
                if(size.compareAndSet(oldSize, newSize)) {
                    for(;;) {
                        int oldHead = head.get();
                        int newHead = oldHead + 1;
                        result = a.get(oldHead);
                        if(tail.compareAndSet(oldHead, newHead)){
                            return result;
                        }
                    }
                }
            }
        }
    }
}

import java.util.concurrent.TimeUnit;
import static java.lang.System.out;

public class Test {

    public static void main(String[] args) {

        OptimisticArrayQueue2<Integer> q = new OptimisticArrayQueue2<Integer>();
        Producer p1 = new Producer(0, q);
        Producer p2 = new Producer(1000, q);
        Consumer c1 = new Consumer(q);
        Consumer c2 = new Consumer(q);
        p1.start();
        p2.start();
        c1.start();
        c2.start();
    }

    static class Producer extends Thread {

        private int offset;
        private int number;
        private OptimisticArrayQueue2 q;

        public Producer(int number, int offset, OptimisticArrayQueue2 q) {
            this.offset = offset;
            this.q = q;
            this.number = number;
        }

        @Override
        public void run() {
            for(int i = 0 + offset; i < offset + 1000; i++) {
                out.println("[]");
                q.enq(i);
                TimeUnit.MILLISECONDS.sleep();
            }
        }
    }

    static class Consumer extends Thread {

        private int offset;
        private OptimisticArrayQueue2 q;

        public Consumer(int offset, OptimisticArrayQueue2 q) {
            this.offset = offset;
            this.q = q;
        }

        @Override
        public void run() {
            for(int i = 0 + offset; i < offset + 1000; i++) {
                int val = q.deq();
                TimeUnit.MILLISECONDS.sleep();
            }
        }
    }
}

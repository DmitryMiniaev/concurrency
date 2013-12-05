import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import static java.lang.System.out;

public class Test {

    private static Random rnd = new Random();

    public static void main(String[] args) throws InterruptedException {


        OptimisticArrayQueue2<Integer> q = new OptimisticArrayQueue2<Integer>(300);
        Producer p1 = new Producer(0, 0, q);
        Producer p2 = new Producer(1, 1000, q);
        Consumer c1 = new Consumer(0, q);
        Consumer c2 = new Consumer(1, q);
        out.println(String.format("[Start]"));
        p1.start();
        p2.start();
        c1.start();
        c2.start();
        p1.join();
        p2.join();
        c1.join();
        c2.join();
        out.println(String.format("[End]"));
    }

    static class Producer extends Thread {
        private int offset;
        private int number;
        private OptimisticArrayQueue2<Integer> q;

        public Producer(int number, int offset, OptimisticArrayQueue2<Integer> q) {
            this.offset = offset;
            this.q = q;
            this.number = number;
        }

        @Override
        public void run() {
            for(int i = 0 + offset; i < offset + 100; i++) {
                q.enq(i);
                out.println(String.format("[Producer #%s] generate %s", number, i));
                try {
                    TimeUnit.MILLISECONDS.sleep(rnd.nextInt(200));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static class Consumer extends Thread {

        private int number;
        private OptimisticArrayQueue2<Integer> q;
        private static int cnt = 0;
        public Consumer(int number, OptimisticArrayQueue2<Integer> q) {
            this.number = number;
            this.q = q;
        }

        @Override
        public void run() {
            List<Integer> acc = new ArrayList<>();
            for(; cnt < 200 ; ) {
                Integer val = q.deq();
                if(val == null) continue;
                acc.add(val);
                try {
                    TimeUnit.MILLISECONDS.sleep(rnd.nextInt(200));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                cnt++;
                out.println(String.format("[Consumer #%s] process %s", number, val));
            }
            Collections.sort(acc);
            out.println(acc);
        }
    }
}

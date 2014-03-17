import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

public class CASStack {

    static class Stack<T> {

        static class Node<T> {
            Node<T> prev;
            T data;

            public Node(T data) {
                this.data = data;
            }
        }

        AtomicReference<Node<T>> top = new AtomicReference<Node<T>>();

        T pop() {
            for (;;) {
                Node<T> old = top.get();
                if(old == null) return null;
                if(top.compareAndSet(old, old.prev)) {
                    return old.data;
                }
            }
        }

        void push(T data) {
            Node<T> n = new Node<T>(data);
            for(;;) {
                n.prev = top.get();
                if(top.compareAndSet(n.prev, n)) {
                    return;
                }
            }
        }
    }

    static Stack<Integer> st = new Stack<Integer>();

    public static void main(String[] args) throws InterruptedException {
        final Queue<Integer> q = new ConcurrentLinkedQueue<Integer>();
        final Random rnd = new Random();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 1000; i++) {
                    try {
                        Thread.sleep(10 + rnd.nextInt(50));
                    } catch (InterruptedException e) {
                    }
                    st.push(i);
                }
                Integer res = st.pop();
                while (res != null) {
                    q.add(res);
                    res = st.pop();
                }
            }
        };
        Thread t1 = new Thread(r);
        Thread t2 = new Thread(r);
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        while (!q.isEmpty()) {
            Integer x = q.remove();
            if(map.containsKey(x)) {
                int cnt = map.get(x);
                map.put(x, ++cnt);
            } else  {
                map.put(x, 1);
            }
        }
        for(int i = 0; i < 100; i++) {
            Integer x = map.get(i);
            if(x == null || x != 2) {
                System.out.println("FAIL");
                return;
            }
        }
        System.out.println("SUCCESS");
    }
}

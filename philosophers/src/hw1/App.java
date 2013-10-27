package hw1;

/**
 * Created with IntelliJ IDEA.
 * User: Dimon
 * Date: 8/12/13
 * Time: 11:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class App {
    static final int COUNT = 10;

    public static void main(String[] args) throws Exception {
        MyPhilosopher[] phils = new MyPhilosopher[COUNT];

        Fork last = new Fork();
        Fork left = last;
        Semaphore semaphore = new Semaphore(COUNT - 1);
        for (int i = 0; i < COUNT; i++) {
            Fork right = (i == COUNT - 1) ? last : new Fork();
            phils[i] = new MyPhilosopher(i, left, right, semaphore);
            left = right;
        }

        Thread[] threads = new Thread[COUNT];
        for (int i = 0; i < COUNT; i++) {
            threads[i] = new Thread(phils[i]);
            threads[i].start();
        }

        Thread.sleep(60000);

        for (MyPhilosopher phil : phils) {
            phil.stopFlag = true;
        }

        for (Thread thread : threads) {
            thread.join();
        }
        for (MyPhilosopher phil : phils) {
            System.out.println("[Philosopher " + phil.position + "] ate " +
                    phil.eatCount + " times and waited " + phil.waitTime + " ms");
        }
    }
}

package hw1;

/**
 * Created with IntelliJ IDEA.
 * User: Dimon
 * Date: 8/12/13
 * Time: 11:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class MyPhilosopher extends Philosopher implements Runnable {

    volatile boolean stopFlag = false;
    private Semaphore semaphore;

    public MyPhilosopher(int position, Fork left, Fork right, Semaphore semaphore) {
        super(position, left, right);
        this.semaphore = semaphore;
    }

    @Override
    public void run() {
        while (!stopFlag) {
            think();
            try{
                semaphore.acquire();
                synchronized (left) {
                    System.out.println("[Philosopher" + position + "] took left fork");
                    synchronized (right) {
                        System.out.println("[Philosopher" + position + "] took right fork");
                        eat();
                    }
                }
            } catch (InterruptedException e) {

            } finally {
                semaphore.release();
            }
            System.out.println("[Philosopher " + position + "] stopped");
        }
    }
}

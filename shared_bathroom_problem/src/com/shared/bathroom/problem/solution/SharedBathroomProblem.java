package com.shared.bathroom.problem.solution;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SharedBathroomProblem {

    private static Bathroom bathroom = new Bathroom();
    private static volatile boolean isStopped = false;
    private static Random rnd = new Random();

    public static void main(String[] args) throws InterruptedException {
        System.out.println("[Start]");
        Producer maleProducer = new Producer();
        maleProducer.start();
        TimeUnit.SECONDS.sleep(10);
        isStopped = true;
        maleProducer.join();
        System.out.println("[End]");
    }

    protected static class Producer extends Thread {

        @Override
        public void run() {
            try {
                List<Thread> threadList = new ArrayList<Thread>();
                while (!isStopped) {
                    if (rnd.nextBoolean()) {
                        Male male = new Male();
                        threadList.add(male);
                        male.start();
                    } else {
                        Female female = new Female();
                        threadList.add(female);
                        female.start();
                    }
                    TimeUnit.MILLISECONDS.sleep(10);
                }
                for (Thread t : threadList) {
                    t.join();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected static class Female extends Thread {
        @Override
        public void run() {
            Bathroom.FemaleLock femaleLock = bathroom.getFemaleLock();
            try {
                femaleLock.enterFemale();
                TimeUnit.MILLISECONDS.sleep(rnd.nextInt(100));
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                femaleLock.leaveFemale();
            }
        }
    }

    protected static class Male extends Thread {
        @Override
        public void run() {
            Bathroom.MaleLock maleLock = bathroom.getMaleLock();
            try {
                maleLock.enterMale();
                TimeUnit.MILLISECONDS.sleep(rnd.nextInt(100));
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                maleLock.leaveMale();
            }
        }
    }

//    void brushTeeth(int cnt) throws InterruptedException {
//        System.out.println("[Female # " + cnt + " starts teeth brushing]");
//        TimeUnit.MILLISECONDS.sleep(rnd.nextInt(100));
//        System.out.println("[Female # " + cnt + " end teeth brushing]");
//    }

    protected static class Bathroom {

        private enum UsedBy {
            NO_ONE,
            MALES,
            FEMALES
        }

        private int males = 0;
        private int females = 0;
        private boolean maleTurn = false;
        private boolean femaleTurn = false;
        private UsedBy usedBy = UsedBy.NO_ONE;

        private Lock lock = new ReentrantLock();
        private Condition maleCondition = lock.newCondition();
        private Condition femaleCondition = lock.newCondition();
        private MaleLock malelock = new MaleLock();
        private FemaleLock femaleLock = new FemaleLock();

        public MaleLock getMaleLock() {
            return malelock;
        }

        public FemaleLock getFemaleLock() {
            return femaleLock;
        }

        protected class MaleLock {

            public void enterMale() throws InterruptedException {
                lock.lock();
                try {
                    maleTurn = true;
                    while (femaleTurn) {
                        maleCondition.await();
                    }
                    while (usedBy == UsedBy.FEMALES) {
                        maleCondition.await();
                    }
                    usedBy = UsedBy.MALES;
                    males++;
                    System.out.println("[Male # " + males + " enter bathroom]");
                } finally {
                    lock.unlock();
                }
            }

            public void leaveMale() {
                lock.lock();
                try {
                    System.out.println("[Male # " + males + " leave bathroom]");
                    males--;
                    if (males == 0) {
                        maleTurn = false;
                        usedBy = UsedBy.NO_ONE;
                        System.out.println("[All males leave bathroom]");
                        femaleCondition.signalAll();
                    }
                } finally {
                    lock.unlock();
                }
            }
        }

        protected class FemaleLock {

            public void enterFemale() throws InterruptedException {
                lock.lock();
                try {
                    while(maleTurn) {
                        femaleCondition.await();
                    }
                    femaleTurn = true;
                    while (usedBy == UsedBy.MALES) {
                        femaleCondition.await();
                    }
                    usedBy = UsedBy.FEMALES;
                    females++;
                    System.out.println("[Female # " + females + " enter bathroom]");
                } finally {
                    lock.unlock();
                }
            }

            public void leaveFemale() {
                lock.lock();
                try {
                    System.out.println("[Female # " + females + " leave bathroom]");
                    females--;
                    if (females == 0) {
                        femaleTurn = false;
                        usedBy = UsedBy.NO_ONE;
                        System.out.println("[All females leave bathroom]");
                        maleCondition.signalAll();
                    }
                } finally {
                    lock.unlock();
                }
            }
        }
    }
}

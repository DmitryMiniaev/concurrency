package com.concurrency.homework;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BFS {

    private final Graph graph;
    private final ExecutorService pool;
    private final int THREADS_NUM;
    private final int MAX_DEPTH;
    private final CountDownLatch latch;
    private final ConcurrentMap<Integer, Boolean> used = new ConcurrentHashMap<Integer, Boolean>();
    private final Object monitor = new Object();
    private LinkedBlockingQueue<Integer> curQ = new LinkedBlockingQueue<Integer>();
    private LinkedBlockingQueue<Integer> nextQ = new LinkedBlockingQueue<Integer>();
    final AtomicIntegerArray depth;
    private int awaitin = 0;
    private int lvl = 0;
    private boolean barrier = true;
    private final Lock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();

    public static void main(String[] args) {
        System.out.println("[Creating graph...]");
        int n = 20;
        Graph graph = new Graph(n, n);
        int threadsNum = Runtime.getRuntime().availableProcessors();
        int maxDepth = n;
        BFS bfs = new BFS(graph, threadsNum, maxDepth, n);
        System.out.println("[Concurrent BFS starts]");
        long startTime = System.currentTimeMillis();
        bfs.start();
        long endTime = System.currentTimeMillis() - startTime;
        System.out.println("[Concurrent BFS ends. Total time: " + TimeUnit.MILLISECONDS.toMillis(endTime) + " ms ]");
        System.out.println("[Linear BFS starts]");
        startTime = System.currentTimeMillis();
        int[] depth2 = bfs(graph, n, maxDepth, 0);
        endTime = System.currentTimeMillis() - startTime;
        System.out.println("[Linear BFS ends. Total time: " + TimeUnit.MILLISECONDS.toMillis(endTime) + " ms ]");
        int cnt = 0;
        for(int i = 0; i < n; i++) {
            if(depth2[i] != bfs.depth.get(i)) {
                cnt++;
            }
        }
        if(cnt != 0) {
            System.out.println("Wrong diff");
        }
    }

    public BFS(Graph graph, int threadNum, int maxDepth, int n) {
        this.graph = graph;
        THREADS_NUM = threadNum;
        MAX_DEPTH = maxDepth;
        pool = Executors.newFixedThreadPool(THREADS_NUM);
        latch = new CountDownLatch(THREADS_NUM + 1);
        depth = new AtomicIntegerArray(new int[n]);
    }

    public void start() {
        try {
            curQ.add(0);
            used.put(0, true);
            for (int i = 0; i < THREADS_NUM; i++) {
                pool.execute(new SearchTask(i));
            }
            latch.countDown();
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            pool.shutdown();
        }
    }

    protected class SearchTask implements Runnable {
        private int name;

        public SearchTask(int name) {
            this.name = name;
        }

        @Override
        public void run() {
            System.out.println("[Thread # " + name + " start execution]");
            try {
                while (true) {
                    Integer u = curQ.poll();
                    if (u != null) {
                        List<Integer> adj = graph.getAdj(u);
                        TimeUnit.MILLISECONDS.sleep(10);
                        for (Integer v : adj) {
                            if(used.putIfAbsent(v, true) == null) {
                                nextQ.add(v);
                                depth.set(v, lvl + 1);
                            }
                        }
                    } else  {
                        System.out.println("[Thread # " + name + " is arrived to cs:");
                        //magic here
                        synchronized (monitor) {
                            awaitin++;
                            if(awaitin == THREADS_NUM) {
                                curQ = new LinkedBlockingQueue<Integer>(nextQ);
                                lvl = curQ.isEmpty() ? MAX_DEPTH : lvl + 1;
                                nextQ = new LinkedBlockingQueue<Integer>();
                                barrier = false;
                                awaitin = 0;
                                monitor.notifyAll();
                            } else {
                                while (barrier) {
                                    monitor.wait();
                                }
                            }
                        }
                        System.out.println("[Thread # " + name + " exit cs:");
                        if(lvl == MAX_DEPTH) {
                            break;
                        }
                    }
                }
            } catch (InterruptedException e) {

            } finally {
                System.out.println("[Thread # " + name + " finish execution]");
                latch.countDown();
            }
        }
    }

    static class Graph {

        protected List<Integer>[] g;

        public Graph(int min, int max) {
            int size = min + (int) (Math.random() * (max - min));
            g = new ArrayList[size];
            for (int i = 0; i < size; i++) {
                g[i] = new ArrayList<Integer>();
            }

            int step = (int) Math.sqrt(size);
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j+=step + 1 + (Math.random() * 11)) {
                    g[i].add(j);
                }
            }
        }

        public List<Integer> getAdj(int u) {
            return (u < g.length) ? g[u]  : null;
        }
    }

    static int[] bfs(Graph g, int n, int d, int s) {
        boolean[] used = new boolean[n];
        int[] depth = new int[n];
        Queue<Integer> q = new LinkedList<Integer>();
        q.add(s);
        used[s] = true;
        while (!q.isEmpty()){
            int u = q.remove();
            for(Integer v : g.getAdj(u)) {
                if(!used[v] && depth[v] < d){
                    q.add(v);
                    used[v] = true;
                    depth[v] = depth[u] + 1;
                }
            }
        }
        return depth;
    }
}
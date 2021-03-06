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
    private final AtomicIntegerArray depth;
    private final ConcurrentMap<Integer, Boolean> used = new ConcurrentHashMap<Integer, Boolean>();
    private LinkedBlockingQueue<Integer> curQ = new LinkedBlockingQueue<Integer>();
    private LinkedBlockingQueue<Integer> nextQ = new LinkedBlockingQueue<Integer>();
    private volatile int lvl = 0; // really need this???
    private CyclicBarrier barrier;
 
    public static void main(String[] args) {
        System.out.println("[Creating graph...]");
        int n = 100;
        Graph graph = new Graph(n, n);
        int threadsNum = Runtime.getRuntime().availableProcessors();
        int maxDepth = n;
        BFS bfs1 = new BFS(graph, threadsNum, maxDepth, n);
        System.out.println("[Concurrent BFS starts]");
        long startTime = System.currentTimeMillis();
        bfs1.start();
        long endTime = System.currentTimeMillis() - startTime;
        System.out.println("[Concurrent BFS ends. Total time: " + TimeUnit.MILLISECONDS.toMillis(endTime) + " ms ]");
        System.out.println("[Linear BFS starts]");
        startTime = System.currentTimeMillis();
        int[] depth1 = bfs1.getDepth();
        int[] depth2 = bfs2(graph, n, maxDepth, 0);
        endTime = System.currentTimeMillis() - startTime;
        System.out.println("[Linear BFS ends. Total time: " + TimeUnit.MILLISECONDS.toMillis(endTime) + " ms ]");
        int cnt = 0;
        for(int i = 0; i < n; i++) {
            if(depth1[i] != depth2[i]) {
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
        barrier = new CyclicBarrier(THREADS_NUM);
    }
 
    public int[] getDepth() {
    	int n = depth.length();
    	int[] a = new int[n];
    	for(int i = 0; i < n; i++) {
    		a[i] = depth.get(i);
    	}
    	return a;
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
//                        TimeUnit.MILLISECONDS.sleep(10);
                        for (Integer v : adj) {
                            if(used.putIfAbsent(v, true) == null) {
                                nextQ.add(v);
                                depth.set(v, depth.get(u) + 1);
                            }
                        }
                    } else  {
                        System.out.println("[Thread # " + name + " is arrived to cs:");
                        //magic here
                        int num = barrier.getNumberWaiting();
                        if(num == THREADS_NUM - 1) {
                        	//broken barrier exception??
                              lvl = nextQ.isEmpty() ? MAX_DEPTH : lvl + 1;
                              LinkedBlockingQueue<Integer> tmp = curQ;
                              curQ = nextQ;
                              nextQ = tmp; 
                              barrier.await();
                              barrier.reset();
                        }else {
                        	barrier.await();
                        }
                        System.out.println("[Thread # " + name + " exit cs:");
                        if(lvl == MAX_DEPTH) {
                            break;
                        }
                    }
                }
            } catch (InterruptedException | BrokenBarrierException e) {
 
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
 
    static int[] bfs2(Graph g, int n, int d, int s) {
        boolean[] used = new boolean[n];
        int[] depth = new int[n];
        Queue<Integer> q = new LinkedList<Integer>();
        q.add(s);
        used[s] = true;
        while (!q.isEmpty()){
            int u = q.remove();
//            try {
//				TimeUnit.MILLISECONDS.sleep(10);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
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
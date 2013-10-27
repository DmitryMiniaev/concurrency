package task1;

public class MultithreadIntegration {

    protected static int MAX_THREADS = 10;

    public static void main(String[] args) throws InterruptedException {
        int leftBound = 1;
        int rightBound = 3;
        double intervalLength = (rightBound - leftBound) / (double)MAX_THREADS;
        Thread[] threads = new Thread[MAX_THREADS];
        IntegrationJob[] jobs = new IntegrationJob[MAX_THREADS];
        for (int i = 0; i < MAX_THREADS; i++) {
              jobs[i] = new IntegrationJob(leftBound + i * intervalLength, leftBound + (i + 1) * intervalLength );
              threads[i] = new Thread(jobs[i]);
              threads[i].start();
        }
        double integral = 0.0;
        for(int i = 0; i < MAX_THREADS; i++) {
            threads[i].join();
            integral += jobs[i].getResult();
        }
        System.out.println(integral);
    }
}

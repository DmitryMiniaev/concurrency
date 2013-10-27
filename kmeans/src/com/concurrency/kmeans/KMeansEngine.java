package com.concurrency.kmeans;

public class KMeansEngine {

    private static final double EPS = 1e10;
    //vector dimension
    private static final int D = 2;

    private final int K;
    private final int N;

    private double[][] centers;
    private double[][] vectors;

    public KMeansEngine(int k, double[][] initCentroidz, double[][] vectors) {
        this.centers = initCentroidz.clone();
        this.vectors = vectors.clone();
        this.K = k;
        this.N = vectors.length;
    }

    private double euclidDistanceSquare(double[] a, double[] b) {
        double x = a[0] - b[0];
        double y = a[1] - b[1];
        return x * x + y * y;
    }

    private boolean approximatelyEqual(double a, double b, double epsilon){
        return Math.abs(a - b) <= ((Math.abs(a) < Math.abs(b) ? Math.abs(b) : Math.abs(a)) * epsilon);
    }
    public boolean isGreaterThan(double a, double b, double epsilon){
        return (a - b) > ((Math.abs(a) < Math.abs(b) ? Math.abs(b) : Math.abs(a)) * epsilon);
    }

    public double[][] doKMeans() {
        //http://en.wikipedia.org/wiki/K-means_clustering#Standard_algorithm

        boolean canBeUpdated = true;
        while(canBeUpdated) {

            //closest centers
            for(int i = 0; i < N; i++) {
                double min = Double.POSITIVE_INFINITY;
                int color = -1;
                for(int k = 0; k < K; k++) {
                    double dist = euclidDistanceSquare(vectors[i], centers[k]);
                    if(isGreaterThan(min, dist, EPS)) {
                        min = dist;
                        color = k;
                    }
                }
                vectors[i][D + 1] = color;
            }

            //sum by color and dimension
            double[][] sum = new double[K][D];
            for(int i = 0; i < N; i++) {
                int color = (int) vectors[i][D + 1];
                for(int d = 0; d < D; d++) {
                    sum[color][d] += vectors[i][d];
                }
            }

            //new centers
            for(int k = 0; k < K; k++) {
                for(int d = 0; d < D; d++) {
                    double old = centers[k][d];
                    centers[k][d] = 1 / Math.abs(centers[k][d]) * sum[k][d];
                    canBeUpdated |= approximatelyEqual(old, centers[k][d], EPS);
                }
            }
        }
        return vectors;
    }
}

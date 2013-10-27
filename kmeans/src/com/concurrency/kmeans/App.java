package com.concurrency.kmeans;


import com.concurrency.utils.StdDraw;

import java.awt.*;

public class App {

    private static Color[] COLORS = new Color[]{StdDraw.RED, StdDraw.GREEN,
                                                StdDraw.ORANGE, StdDraw.PINK,
                                                StdDraw.BLACK};
    public static void main(String[] args) {
        int n = 100;
        int k = 3;
        int d = 2;
        int min = 1;
        int max = 1;

        StdDraw.setCanvasSize(1300, 650);
        StdDraw.setXscale(0, 1000);
        StdDraw.setYscale(0, 1000);

        double[][] vectors = new double[1000][d + 1];

        for (int i = 0; i < n; i++) {
//            vectors[i][0] =
//            vectors[i][1] =
        }

        double[][] centers = new double[k][d + 1];
        for(int i = 0; i < n; i++) {
//            centers[i][0] = rnd
//            centers[i][1] = rnd
        }

        KMeansEngine engine = new KMeansEngine(k, centers, vectors);
        double[][] result =  engine.doKMeans();
        for(int i = 0; i < n; i++) {
            double x = result[i][0];
            double y = result[i][1];
            Color color = COLORS[((int) result[i][k])];
            StdDraw.setPenColor(color);
            StdDraw.filledCircle(x, y, 1);
        }
    }
}

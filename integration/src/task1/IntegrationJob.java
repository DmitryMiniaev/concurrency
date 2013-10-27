package task1;

/**
 * Created with IntelliJ IDEA.
 * User: dminyaev
 * Date: 17.07.13
 * Time: 19:18
 * To change this template use File | Settings | File Templates.
 */
public class IntegrationJob implements Runnable {

    private static final double EPS = 0.000001;
    private static final int N = 400;

    private double leftBound;
    private double rightBound;
    private double result;

    private static double f(double x) {
        return (9 - x*x);
    }

    static double trapezoid(double a, double b){
        double sum = f(a) + f(b);
        double step = (b - a) / N;
        for(double i = a + step; Math.abs(b - i) > EPS; i += step) {
            sum += 2 * f(i);
        }
        return step * sum * 0.5;
    }

    public IntegrationJob(double aLeftBound, double aRightBound) {
        leftBound = aLeftBound;
        rightBound = aRightBound;
    }

    @Override
    public void run() {
        result = trapezoid(leftBound, rightBound);
    }

    public double getResult() {
        return result;
    }
}

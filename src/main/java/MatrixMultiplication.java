import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.stream.IntStream;

public class MatrixMultiplication {

    static int matrixDimension = 1000;
    private static class MultiplicationFutureTask extends RecursiveTask<Integer> {
        private final int[] list1;
        private final int[] list2;
        private int list_length = 0;
        private int value;

        MultiplicationFutureTask(final int[] list1, final int[] list2, int n) {
            this.list1 = list1;
            this.list2 = list2;
            this.list_length = n;
        }

        public double getValue() {
            return value;
        }

        @Override
        protected Integer compute() {
//            System.out.println("Running compute task for SumArrayFutureTask on Thread: " + Thread.currentThread().getName());
            for(int i = 0; i < list_length; i++)
                value += list1[i]*list2[i];
            return value;
        }
    }

    public static void sequentialMatrixMultiply(int[][] A, int[][] B, int n) {
        long startTime = System.currentTimeMillis();
        int[][] C = getMatrix(matrixDimension, matrixDimension);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = 0;
                for (int k = 0; k < n; k++) {
                    C[i][j] += A[i][k] * B[k][j];
                }
            }
        }
        long timeInNanoSeconds = System.currentTimeMillis() - startTime;
        System.out.println("Sequential Matrix Multiply -> Time taken = " + timeInNanoSeconds + " with, C[0][0] = " + C[0][0]);
    }


    public static void parallelMatrixMultiply(int[][] A, int[][] B, int n) {
        int[][] C = new int[n][n];
        MultiplicationFutureTask[] multiplicationFutureTasks = new MultiplicationFutureTask[n*n];

        long startTime = System.currentTimeMillis();

        int k = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = 0;
                int finalJ = j;
                MultiplicationFutureTask multiplicationFutureTask = new MultiplicationFutureTask(A[i],
                            IntStream.range(0, n).map(a -> B[a][finalJ]).toArray(), n);
                multiplicationFutureTasks[k] = multiplicationFutureTask;
                k++;
            }
        }
        ForkJoinTask.invokeAll(multiplicationFutureTasks);
        k = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = multiplicationFutureTasks[k].join();
                k++;
            }
        }
        long timeInNanoSeconds = System.currentTimeMillis() - startTime;
        System.out.println("Sequential Matrix Multiply -> Time taken = " + timeInNanoSeconds + " with, C[0][0] = " + C[0][0]);
    }

    public static int[][] getMatrix(int rows, int cols) {
        Random random = new Random();

        int[][] matrix = new int[rows][];

        for (int i = 0; i < rows; i++) {
            matrix[i] = new int[cols];
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = random.nextInt(101);
            }
        }
        return matrix;
    }

    public static void main(String[] args) {
        int[][] A = getMatrix(matrixDimension, matrixDimension);
        int[][] B = getMatrix(matrixDimension, matrixDimension);
        sequentialMatrixMultiply(A, B, matrixDimension);
        parallelMatrixMultiply(A, B, matrixDimension);
    }

}

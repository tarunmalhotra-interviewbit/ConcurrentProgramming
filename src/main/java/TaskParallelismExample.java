import java.util.Random;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

public final class TaskParallelismExample {
    private TaskParallelismExample() {
    }

    protected static double seqArraySum(final double[] input) {
        double sum = 0;
        final double startTime = System.currentTimeMillis();
        // Compute sum of array elements
        for (int i = 0; i < input.length; i++) {
            sum += input[i];
        }
        final double endTime = System.currentTimeMillis();
        double duration = (endTime - startTime) ;
        System.out.println(String.format("took: %f milliseconds, sum: %f: seqArraySum", duration, sum));
        return sum;
    }

    protected static double seqDivideAndConquerArraySum(final double[] input) {
        double sum1, sum2;
        sum1 = 0;
        sum2 = 0;
        final double startTime = System.currentTimeMillis();

        // Compute sum of array elements
        for (int i = 0; i < input.length/2; i++) {
            sum1 += input[i];
        }
        for (int i = input.length/2; i < input.length; i++) {
            sum2 += input[i];
        }

        double sum = sum1 + sum2;
        final double endTime = System.currentTimeMillis();
        double duration = (endTime - startTime);
        System.out.println(String.format("took: %f milliseconds, sum: %f: seqDivideAndConquerArraySum", duration, sum));
        return sum;
    }

    private static int getChunkSize(final int nChunks, final int nElements) {
        // Integer ceil
        return (nElements + nChunks - 1) / nChunks;
    }

    private static int getChunkStartInclusive(final int chunk,
                                              final int nChunks, final int nElements) {
        final int chunkSize = getChunkSize(nChunks, nElements);
        return chunk * chunkSize;
    }

    private static int getChunkEndExclusive(final int chunk, final int nChunks,
                                            final int nElements) {
        final int chunkSize = getChunkSize(nChunks, nElements);
        final int end = (chunk + 1) * chunkSize;
        if (end > nElements) {
            return nElements;
        } else {
            return end;
        }
    }

    private static class ArraySumTask extends RecursiveAction {
        private final int startIndexInclusive;
        private final int endIndexExclusive;
        private final double[] input;
        private double value;

        ArraySumTask(final int setStartIndexInclusive,
                     final int setEndIndexExclusive, final double[] setInput) {
            this.startIndexInclusive = setStartIndexInclusive;
            this.endIndexExclusive = setEndIndexExclusive;
            this.input = setInput;
        }

        public double getValue() {
            return value;
        }

        @Override
        protected void compute() {
            for(int i = startIndexInclusive; i < endIndexExclusive; i++)
                value += input[i];
        }
    }

    private static class ArraySumFutureTask extends RecursiveTask<Double> {
        private final int startIndexInclusive;
        private final int endIndexExclusive;
        private final double[] input;
        private double value;

        ArraySumFutureTask(final int setStartIndexInclusive,
                           final int setEndIndexExclusive, final double[] setInput) {
            this.startIndexInclusive = setStartIndexInclusive;
            this.endIndexExclusive = setEndIndexExclusive;
            this.input = setInput;
        }

        public double getValue() {
            return value;
        }

        @Override
        protected Double compute() {
            for(int i = startIndexInclusive; i < endIndexExclusive; i++)
                value += input[i];
            return value;
        }
    }


    protected static double parArraySum(final double[] input) {
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "1");
        assert input.length % 2 == 0;
        final double startTime = System.currentTimeMillis();
        int mid = input.length / 2;
        ArraySumTask sumTaskLeft = new ArraySumTask(0, mid, input);
        ArraySumTask sumTaskRight = new ArraySumTask(mid, input.length, input);
        //sumTaskLeft.fork(); //creating a new thread and asking that thread to "compute"
        sumTaskRight.fork(); //main
        sumTaskLeft.compute();
        sumTaskRight.join(); //main + block call
        double result = sumTaskLeft.getValue() + sumTaskRight.getValue();
        final double endTime = System.currentTimeMillis();
        double duration = (endTime - startTime);
        System.out.println(String.format("took: %f milliseconds, sum: %f: parArraySum ", duration, result));
        return result;
    }

    protected static double parManyTaskArraySum(final double[] input,
                                                final int numTasks) {
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "4");
        final double startTime = System.currentTimeMillis();
        int taskCount = numTasks;
        if(numTasks > input.length)
            taskCount = input.length;
        double sum = 0;
        ArraySumTask[] sumTaskList = new ArraySumTask[taskCount];
        for(int i = 0; i<taskCount; i++) {
            ArraySumTask arraySumTask = new ArraySumTask(getChunkStartInclusive(i, taskCount, input.length), getChunkEndExclusive(i, taskCount, input.length), input);
            sumTaskList[i] = arraySumTask;
        }
        ForkJoinTask.invokeAll(sumTaskList);
        for (ArraySumTask sumTask : sumTaskList) {
            sum += sumTask.getValue();
        }
        final double endTime = System.currentTimeMillis();
        double duration = (endTime - startTime);
        System.out.println(String.format("took: %f milliseconds, sum: %f, numTasks: %d: parManyTaskArraySum ", duration, sum, taskCount));
        return sum;
    }

    protected static double parManyTaskArrayFutureSum(final double[] input,
                                                      final int numTasks) {
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "1000");
        final double startTime = System.currentTimeMillis();
        int taskCount = numTasks;
        if(numTasks > input.length)
            taskCount = input.length;
        double sum = 0;
        ArraySumFutureTask[] sumTaskList = new ArraySumFutureTask[taskCount];
        for(int i = 0; i<taskCount; i++) {
            ArraySumFutureTask arraySumTask = new ArraySumFutureTask(getChunkStartInclusive(i, taskCount, input.length), getChunkEndExclusive(i, taskCount, input.length), input);
            sumTaskList[i] = arraySumTask;
        }
        ForkJoinTask.invokeAll(sumTaskList);
        for (ArraySumFutureTask sumTask : sumTaskList) {
            sum += sumTask.join();
        }
        final double endTime = System.currentTimeMillis();
        double duration = (endTime - startTime);
        System.out.println(String.format("took: %f milliseconds, sum: %f, numTasks: %d: parManyTaskArrayFutureSum ", duration, sum, taskCount));
        return sum;
    }

    // RUNNER CODE
    private static double[] createArray(final int N) {
        final double[] input = new double[N];
        final Random rand = new Random(314);

        for (int i = 0; i < N; i++) {
            input[i] = rand.nextInt(100);
            // Don't allow zero values in the input array to prevent divide-by-zero
            if (input[i] == 0.0) {
                i--;
            }
        }

        return input;
    }

    public static void main(String[] args) throws InterruptedException {
        final double[] input = TaskParallelismExample.createArray(100000000);
        int runIndex = 1;
        while (runIndex<=10) {
            System.out.println("Run Number "+runIndex);
            TaskParallelismExample.seqArraySum(input);
            TaskParallelismExample.seqDivideAndConquerArraySum(input);
            TaskParallelismExample.parArraySum(input);
            TaskParallelismExample.parManyTaskArraySum(input, 10000);
            //TaskParallelismExample.parManyTaskArraySum(input, 100);
            //TaskParallelismExample.parManyTaskArrayFutureSum(input, 10);
            //TaskParallelismExample.parManyTaskArrayFutureSum(input, 100);
            System.out.println();
            Thread.sleep(1000);
            runIndex++;
        }
    }
}

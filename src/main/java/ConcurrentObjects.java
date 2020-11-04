import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class ConcurrentObjects {

    private static int value = 0;

    static class ParallelAddition  implements Runnable {
        int array[];
        int start, end;
        ReentrantLock lock;
        public ParallelAddition(int array[], int start, int end, ReentrantLock sharedLock){
            this.array = array;
            this.start = start;
            this.end = end;
            this.lock = sharedLock;
        }

        private void add(int operand) {
            boolean done = false;
            while (!done) {
                boolean ans = lock.tryLock();
                if (ans) {
                    value = value + operand;
                    lock.unlock();
                    done = true;
                }
            }
        }

        public void run() {
            for (int i = start; i < end; i++) {
                    add(calculate((array[i])));
            }
        }
    }

    public static int serialArraySum(int[] array, int size, int offset){
        int sum = offset;
        for(int i=0; i<size; i++){
            sum = sum + calculate(array[i]);
        }
        return sum;
    }

    public static int parallelArraySumUsingAtomicInteger(int[] array, int size, int offset, int threadCount){
        AtomicInteger sum = new AtomicInteger(offset);
        Thread[] threads = new Thread[threadCount];

        for(int threadNum = 0; threadNum < threadCount; threadNum++){
            int start = (size*threadNum)/threadCount;
            int end = start + size/threadCount;
            threads[threadNum] = new Thread(() -> {
                for(int i=start; i<end; i++){
                    sum.set(sum.get() + array[i]);
                    //sum.addAndGet(calculate(array[i]));
                }
            });
            threads[threadNum].start();
        }
        for (int threadNum = 0; threadNum < threadCount; threadNum++){
            try {
                threads[threadNum].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return sum.get();
    }


    public static int[] getIntegerSequence(int size){
        int[] array = new int[size];
        Random random = new Random();
        //Create random array
        for(int i=0; i<size; i++){
            array[i] = random.nextInt(10);
        }
        return array;
    }

    //pessimistic locks
    public static int parallelArraySum(int[] array, int size, int offset, int threadCount){
        Thread[] threads = new Thread[threadCount];
        ReentrantLock lock = new ReentrantLock();
        for(int index =0; index < threadCount; index++){
            threads[index] = new Thread(new ParallelAddition(array, (size*index)/threadCount, (size*(index+1))/threadCount, lock));
            threads[index].start();
        }
        for (int index =0; index < 4; index++){
            try {
                threads[index].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return value;
    }
    public static int calculate(int input){
        return (int)((Math.sqrt(input)*(input/100)+Math.pow(input,3)-Math.sqrt(Math.sqrt(input))+Math.pow(10,3)+Math.pow(4,7))/100);
    }

    public static void sanityCheck(int serial, int parallel){
        if(serial>parallel)
            throw new RuntimeException("Parallel sum is less than the correct sum...");
        if(serial<parallel)
            throw new RuntimeException("Parallel sum is more than the correct sum...");
    }

    public static void main(String[] args) {
        int arrayLen = 100000000; //100 M
        int offset = 0;
        int[] array = getIntegerSequence(arrayLen);

        long startTime, serialTime, parallelTime, parallelTimeUsingAtomicInteger;
        //serial
        startTime= System.currentTimeMillis();
        int serialAddSum = serialArraySum(array, arrayLen, offset);
        serialTime= System.currentTimeMillis() - startTime;
        //Atomic Integer
        startTime= System.currentTimeMillis();
        int parallelAddSumUsingAtomicInteger = parallelArraySumUsingAtomicInteger(array, arrayLen, offset, 4);
        parallelTimeUsingAtomicInteger= System.currentTimeMillis() - startTime;
        //pessimistic
        startTime= System.currentTimeMillis();
        int parallelAddSum = parallelArraySum(array, arrayLen, offset, 4);
        parallelTime= System.currentTimeMillis() - startTime;
        //time taken
        System.out.println("Serial Execution time = "+serialTime);
        System.out.println("Parallel Execution time using AtomicInteger= "+parallelTimeUsingAtomicInteger);
        System.out.println("Parallel Execution time without AtomicInteger= "+parallelTime);
        System.out.println();
        //sum values
        System.out.println("Serial Sum = " + serialAddSum);
        System.out.println("Parallel Sum with AtomicInteger = " + parallelAddSumUsingAtomicInteger);
        System.out.println("Parallel Sum = " + parallelAddSum);

        sanityCheck(serialAddSum, parallelAddSumUsingAtomicInteger);
        sanityCheck(serialAddSum, parallelAddSum);

       }
}

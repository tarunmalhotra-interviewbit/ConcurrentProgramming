import es.usc.citius.common.parallel.Parallel;

import java.util.Random;
import java.util.concurrent.Phaser;

public class ParallelBarrier {

    public static Object[] initTempArrays(float[] array, int size){
        float[] tempOld = new float[size];
        float[] tempNew = new float[size];
        //init
        for(int i=0; i<size; i++){
            tempOld[i] = array[i];
        }
        tempNew[0] = array[0];
        tempNew[size-1] = array[size-1];
        return new Object[]{tempOld, tempNew};
    }
    public static float[] serialJacobiRelaxation(float[] array, int size, int stepCount){
        float[] tempOld, tempNew;
        Object[] arrays = initTempArrays(array, size);
        tempOld = (float[])arrays[0];
        tempNew = (float[])arrays[1];

        for(int step=1; step<=stepCount; step++){
            for(int index =1; index<size-1; index++){
                tempNew[index] = (tempOld[index-1]+tempOld[index+1])/2.0f;
            }
            float[] temp = tempOld;
            tempOld = tempNew;
            tempNew = temp;
        }
        return tempOld;
    }

    public static float[] parallelJacobiRelaxation(float[] array, int size, int stepCount){
        float[] tempOld, tempNew;
        Object[] arrays = initTempArrays(array, size);
        tempOld = (float[])arrays[0];
        tempNew = (float[])arrays[1];

        Phaser ph = new Phaser(0);
        ph.bulkRegister(size-2);

        Thread[] threads = new Thread[size-2];

        for (int ii = 1; ii < size-1; ii++) { // forParallel
            int j = ii;
            threads[ii-1] = new Thread(() -> {
                float[] threadPrivateOldArr = tempOld;
                float[] threadPrivateNewArr = tempNew;
                for (int iter = 0; iter < stepCount; iter++) {
                    threadPrivateNewArr[j] = (threadPrivateOldArr[j - 1]
                            + threadPrivateOldArr[j + 1]) / 2.0f;

//                    System.out.println("Before: j : " + j + ", step:" + iter + ", threadPrivateNewArr: "
//                            + threadPrivateNewArr + ", threadPrivateOldArr: " + threadPrivateOldArr
//                            + ", oldArr: " + tempOld + ", newArr: " + tempNew);
//                    ph.arriveAndAwaitAdvance(); // BARRIER 1
//
//                    System.out.println("After: j : " + j + ", step:" + iter +", threadPrivateNewArr: "
//                            + threadPrivateNewArr + ", threadPrivateOldArr: " + threadPrivateOldArr
//                    + ", oldArr: " + tempOld + ", newArr: " + tempNew);
                    ph.arriveAndAwaitAdvance(); // BARRIER 2
                    float[] temp = threadPrivateNewArr;
                    threadPrivateNewArr = threadPrivateOldArr;
                    threadPrivateOldArr = temp;
                }
            });
            threads[ii-1].start();
        }

        for (int ii = 0; ii < size-2; ii++) {
            try {
                threads[ii].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if(stepCount%2==0)
            return tempOld;
        return tempNew;

    }

    public static float[] parallelChunkedJacobiRelaxation(float[] array, int size, int stepCount, int tasks){
        float[] tempOld, tempNew;
        Object[] arrays = initTempArrays(array, size);
        tempOld = (float[])arrays[0];
        tempNew = (float[])arrays[1];


        Phaser ph = new Phaser(0);
        ph.bulkRegister(tasks);
        Thread[] threads = new Thread[tasks];

        for (int ii = 0; ii < tasks; ii++) { //forParallel
            final int i = ii;
                // [7 8 9] [73  4 4] [556 2 4] [2 4 5] [6 7 8]
            threads[ii] = new Thread(() -> {
                float[] threadPrivateOldArr = tempOld;
                float[] threadPrivateNewArr = tempNew;

                final int chunkSize = (size - 2 + tasks - 1) / tasks;
                final int left = (i * chunkSize) + 1;
                int right = (left + chunkSize) - 1;
                if (right > size-2) right = size-2;

                for (int iter = 0; iter < stepCount; iter++) {
                    for (int j = left; j <= right; j++) {
                        threadPrivateNewArr[j] = (threadPrivateOldArr[j - 1]
                                + threadPrivateOldArr[j + 1]) / 2.0f;
                    }
                    ph.arriveAndAwaitAdvance();

                    float[] temp = threadPrivateNewArr;
                    threadPrivateNewArr = threadPrivateOldArr;
                    threadPrivateOldArr = temp;
                }
            });
            threads[ii].start();
        }

        for (int ii = 0; ii < tasks; ii++) {
            try {
                threads[ii].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(stepCount%2==0)
            return tempOld;
        return tempNew;
    }

    public static void sanityCheck(float[] arr1, float[] arr2, int n){
        if(arr1.length !=n || arr2.length!=n)
                throw new RuntimeException("Mismatch in expected and given output size...");
        for(int i=0; i<n; i++){
            if(arr1[i]!=arr2[i])
                throw new RuntimeException("Mismatch in the two outputs...");
//            else
//                System.out.print(arr1[i] + ", ");
        }
    }

    public static void main(String[] args) {
        int size = 65535;
        int degrees = 400;
        float[] array = new float[size];
        Random random = new Random();
        //Create random array
        for(int i=0; i<size; i++){
            array[i] = (float)random.nextInt(10);
        }

        long startTime, serialTime, parallelTime, parallelChunkedTime;

        startTime= System.currentTimeMillis();
        float[] resultArraySerial = serialJacobiRelaxation(array, size, degrees);
        serialTime= System.currentTimeMillis() - startTime;
        //startTime= System.currentTimeMillis();
        //float[] resultArrayParallel = parallelJacobiRelaxation(array, size, degrees);
        //parallelTime= System.currentTimeMillis() - startTime;
        startTime= System.currentTimeMillis();
        float[] resultArrayParallelChunked = parallelChunkedJacobiRelaxation(array, size, degrees, 8);
        parallelChunkedTime= System.currentTimeMillis() - startTime;

        System.out.println("Input Array:");
        for(float arrayElem : array){
            System.out.print(arrayElem + ", ");
        }
        //System.out.println("\nOutput Array:");
        //sanityCheck(resultArraySerial, resultArrayParallel, size);
        System.out.println("\nOutput Array:");
        sanityCheck(resultArraySerial, resultArrayParallelChunked, size);
        System.out.println("\nSerial Execution time = "+serialTime);
        //System.out.println("\nParallel Execution time = "+parallelTime);
        System.out.println("\nParallel Chunked Execution time = "+parallelChunkedTime);
    }
}


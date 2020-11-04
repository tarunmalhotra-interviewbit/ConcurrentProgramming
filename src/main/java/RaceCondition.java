import java.util.Random;

public class RaceCondition {

    //cs
    int integer;
    Random random = new Random(System.currentTimeMillis());

    //cs
    void magic(){
        int i = 1000000;
        while(i>0){
            if(integer % 10 == 0){
                //if(integer % 10 !=0){
                    System.out.println(integer);
                //}
            }
            i--;
        }
    }

    //cs
    void magicHelper(){
        int i = 1000000;
        while(i>0) {
            integer = random.nextInt(10000);
            i--;
        }
    }

    public static void test() throws InterruptedException {
        final RaceCondition raceCondition = new RaceCondition();

        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                raceCondition.magic();
            }
        });
        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                raceCondition.magicHelper();
            }
        });

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();
    }

    public static void main(String[] args) {
        try {
            RaceCondition.test();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}

package SenderReceiverSynchronization;


public class Sender implements Runnable {

    private Data data;

    public Sender(Data data) {
        this.data = data;
    }

    public void run() {
        String packets[] = {
                "1st packet",
                "2nd packet",
                "3rd packet",
                "4th packet",
                "End"
        };

        for (String packet : packets) {
            data.send(packet);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e)  {
                Thread.currentThread().interrupt();
                System.out.println("Thread interrupted" + e);
            }
        }
    }
}
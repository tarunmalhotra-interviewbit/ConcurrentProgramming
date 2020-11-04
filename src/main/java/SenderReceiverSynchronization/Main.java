package SenderReceiverSynchronization;

public class Main {
    public static void main(String[] args) {
        Data data = new Data();
        Thread sender = new Thread(new Sender(data));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Thread receiver = new Thread(new Receiver(data));

        sender.start();
        receiver.start();
    }

}

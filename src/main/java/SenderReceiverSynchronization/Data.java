package SenderReceiverSynchronization;

public class Data {
    private String packet;

    // True if receiver should wait
    // False if sender should wait
    private boolean senderToSend = true;

    public synchronized void send(String packet) {
        while (!senderToSend) {
            try {
                wait();
            } catch (InterruptedException e)  {
                Thread.currentThread().interrupt();
                System.out.println("Thread interrupted" + e);
            }
        }
        senderToSend = false;

        this.packet = packet;
        System.out.println(packet + " sent");
        notifyAll();
    }

    public synchronized String receive() {
        while (senderToSend) {
            try {
                wait();
            } catch (InterruptedException e)  {
                Thread.currentThread().interrupt();
                System.out.println("Thread interrupted" + e);
            }
        }
        senderToSend = true;
        System.out.println(packet + " received");
        notifyAll();
        return packet;
    }
}
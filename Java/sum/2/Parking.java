import java.util.concurrent.Semaphore;

class ParkingLot {
    private final Semaphore slots = new Semaphore(10);  // 10 férőhely
    private final Semaphore mutex = new Semaphore(1);   // kritikus szakaszhoz

    public void enter() {
        try {
            slots.acquire(); // Vár, ha nincs hely
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void parking() {
        long start = System.currentTimeMillis();

        // Tidologikus altatás Semaphore segítségével: busy-wait nélkül
        Semaphore pause = new Semaphore(0);

        try {
        // Időzített release: 300ms
        Thread t = new Thread(() -> {
            try {
                Thread.sleep(300);
                pause.release();
            } catch (InterruptedException ignored) {}
        });

        t.start();
        pause.acquire();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void leave() {
        try {
            mutex.acquire(); // csak az itt szükséges művelethez zárunk
            slots.release(); // hely felszabadítása
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            mutex.release();
        }
    }
}

class Car extends Thread {
    private final ParkingLot lot;

    public Car(ParkingLot lot) {
        this.lot = lot;
    }

    public void run() {
        lot.enter();
        lot.parking();
        lot.leave();
    }
}

public class Parking {
    public static void main(String[] args) {
        ParkingLot lot = new ParkingLot();

        for (int i = 0; i < 100; i++) {
            new Car(lot).start();
        }
    }
}

public class ThreadExamples {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 1. Szálindítás ===");

        // Kétféle módon lehet szálat létrehozni:
        Thread thread = new ExampleThread(); // Öröklés Thread osztályból
        Thread runnable = new Thread(new ExampleRunnable()); // Runnable interfész implementálása

        thread.start();    // Szál elindítása
        runnable.start();  // Runnable szál elindítása

        // Kis szünet, hogy az előző szálak biztosan fussanak
        Thread.sleep(200);

        System.out.println("\n=== 2. Szálállapotok ===");

        // NEW állapot: példány létrehozva, de még nem indítottuk el
        Thread t1 = new Thread();
        System.out.println("t1 állapota (NEW): " + t1.getState());

        // RUNNABLE állapot: elindított szál
        Thread t2 = new RunnableStateThread();
        t2.start();

        // TIMED_WAITING: alvó szál (sleep)
        Thread t3 = new SleepStateThread();
        t3.start();

        // Kis várakozás, hogy állapotokat biztosan lássuk
        Thread.sleep(100);

        // Leolvassuk a szálak állapotát
        System.out.println("t2 állapota: " + t2.getState()); // Várhatóan TERMINATED
        System.out.println("t3 állapota: " + t3.getState()); // Várhatóan TIMED_WAITING

        System.out.println("\n=== 3. join() használat ===");

        Thread joinThread = new Thread(() -> {
            System.out.println("Szál fut...");
        });
        joinThread.start();
        joinThread.join(); // Várunk, amíg joinThread be nem fejeződik
        System.out.println("join() után folytatódik a főszál.");

        System.out.println("\n=== 4. Kritikus szakasz szinkronizálása ===");

        int threadCount = Runtime.getRuntime().availableProcessors(); // Általában 4 vagy 8

        SharedBuffer buffer = new SharedBuffer();

        for (int i = 0; i < threadCount; i++) {
            Thread t = new Thread(new BufferTask(buffer));
            t.start();
        }

        // Kis várakozás a szálak végére
        Thread.sleep(1000);
        System.out.println("=== Program vége ===");
    }
}

// ========== 1. példa: Thread és Runnable ==========

class ExampleThread extends Thread {
    @Override
    public void run() {
        System.out.println("Fut: ExampleThread");
    }
}

class ExampleRunnable implements Runnable {
    @Override
    public void run() {
        System.out.println("Fut: ExampleRunnable");
    }
}

// ========== 2. példa: Szálállapotok (Thread.getState()) ==========

class RunnableStateThread extends Thread {
    @Override
    public void run() {
        System.out.println("RunnableStateThread állapota (futás közben): " + this.getState());
    }
}

class SleepStateThread extends Thread {
    @Override
    public void run() {
        try {
            Thread.sleep(2000); // Alvás 2 másodperc → TIMED_WAITING
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

// ========== 4. példa: Kritikus szakasz (synchronized) ==========

class SharedBuffer {

    // Teljes metódus szinkronizálása
    public synchronized void criticalSection() {
        System.out.println("Belépés (metódus): " + Thread.currentThread().getName());
        try {
            Thread.sleep(100); // Kritikus művelet
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Kilépés (metódus): " + Thread.currentThread().getName());
    }

    // Csak egy kódrész szinkronizálása
    public void criticalSectionBlock() {
        synchronized (this) {
            System.out.println("Belépés (blokk): " + Thread.currentThread().getName());
            try {
                Thread.sleep(100); // Kritikus művelet
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Kilépés (blokk): " + Thread.currentThread().getName());
        }
    }
}

class BufferTask implements Runnable {

    private final SharedBuffer buffer;

    public BufferTask(SharedBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public void run() {
        buffer.criticalSection();       // Teljes metódus zárolása
        buffer.criticalSectionBlock();  // Csak egy kódrész zárolása
    }
}

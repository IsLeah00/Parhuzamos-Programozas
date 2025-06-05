import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadingToolsAdvanced {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 1. CyclicBarrier példa ===");

        // Létrehozunk 5 szálat és egy barrier-t, amelyhez mindnek el kell érnie
        CyclicBarrier barrier = new CyclicBarrier(5, () -> {
            // Ez a művelet pontosan egyszer hajtódik végre, amikor mind az 5 szál eléri a gátat
            System.out.println("→ Minden szál megérkezett. Haladunk tovább együtt!");
        });

        for (int i = 0; i < 5; i++) {
            new Thread(new BarrierWorker(barrier), "Dolgozó-" + i).start();
        }

        Thread.sleep(2000);

        System.out.println("\n=== 2. AtomicInteger vs sima int ===");

        AtomicDemo atomicDemo = new AtomicDemo();

        // 10 000 szálat indítunk, mindkettő növel egy értéket
        for (int i = 0; i < 10_000; i++) {
            new Thread(new AtomicWorker(atomicDemo)).start();
        }

        Thread.sleep(2000);

        // Az `int` típusú változó nem szinkronizált, így várhatóan hibás értéket kapunk
        System.out.println("Nem szinkronizált érték: " + atomicDemo.unsafeCounter);
        // Az `AtomicInteger` mindig pontos, mert szálbiztos
        System.out.println("Szinkronizált érték (Atomic): " + atomicDemo.safeCounter.get());

        System.out.println("\n=== 3. Callable interfész és ExecutorService ===");

        ExecutorService executor = Executors.newCachedThreadPool();

        // 10 feladatot adunk le az executor-nak
        for (int i = 0; i < 10; i++) {
            executor.submit(new LightweightTask());
            Thread.sleep(25); // kis szünet, hogy lássuk a kimenetet rendezettebben
        }

        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.SECONDS);

        System.out.println("\n=== 4. Future + invokeAll összegyűjtés ===");

        ExecutorService pool = Executors.newFixedThreadPool(5);

        List<Callable<Integer>> tasks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            tasks.add(new RandomNumberJob());
        }

        List<Future<Integer>> results = pool.invokeAll(tasks);

        int total = 0;
        for (Future<Integer> result : results) {
            try {
                total += result.get(); // minden Future-ből kiszedjük az eredményt
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Végeredmény (összeg): " + total);
        pool.shutdown();
    }
}

// ----------- 1. példa: CyclicBarrier használata -----------

class BarrierWorker implements Runnable {
    private final CyclicBarrier barrier;

    public BarrierWorker(CyclicBarrier barrier) {
        this.barrier = barrier;
    }

    @Override
    public void run() {
        try {
            // Véletlenszerű idő alatt dolgozunk
            System.out.println(Thread.currentThread().getName() + " dolgozik...");
            Thread.sleep(new Random().nextInt(1000));

            // Elérjük a szinkronizációs pontot
            System.out.println(Thread.currentThread().getName() + " elérte a gátat...");
            barrier.await(); // itt várakozunk, amíg minden szál meg nem érkezik

            System.out.println(Thread.currentThread().getName() + " továbbhalad.");
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
    }
}

// ----------- 2. példa: AtomicInteger vs sima int -----------

class AtomicDemo {
    int unsafeCounter = 0; // nem szinkronizált változó
    AtomicInteger safeCounter = new AtomicInteger(0); // szálbiztos változó
}

class AtomicWorker implements Runnable {
    private final AtomicDemo demo;

    public AtomicWorker(AtomicDemo demo) {
        this.demo = demo;
    }

    @Override
    public void run() {
        // Ez a művelet nem atomi – több szál egyidejű növelése hibás értéket adhat
        demo.unsafeCounter++;

        // Ez viszont szálbiztos – garantáltan pontos lesz
        demo.safeCounter.incrementAndGet();
    }
}

// ----------- 3. példa: Callable interfész használata -----------

class LightweightTask implements Callable<Integer> {
    @Override
    public Integer call() {
        try {
            Thread.sleep(100); // szimulált feldolgozás
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName() + " elvégzett egy feladatot.");
        return 1;
    }
}

// ----------- 4. példa: Future értékek összegzése -----------

class RandomNumberJob implements Callable<Integer> {
    @Override
    public Integer call() throws InterruptedException {
        Thread.sleep(500); // időigényes számítás szimulálása
        int num = new Random().nextInt(100); // véletlenszám
        System.out.println(Thread.currentThread().getName() + " visszatér: " + num);
        return num;
    }
}

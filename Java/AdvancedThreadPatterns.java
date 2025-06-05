import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.*;

public class AdvancedThreadPatterns {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 1. BLOCKED állapot demonstrálása ===");

        // Egy közös zárolandó objektum
        Object lock = new Object();

        // Két szál példányosítása – mindkettő ugyanazt a lock-ot próbálja használni
        Thread t1 = new BlockingDemo(lock);
        Thread t2 = new BlockingDemo(lock);

        t1.start();              // t1 megszerzi a lock-ot és elalszik
        Thread.sleep(100);       // biztosítjuk, hogy t1 előbb belépjen
        t2.start();              // t2 itt már nem tud belépni, mert t1 zárolta
        Thread.sleep(200);       // kis idő, hogy megfigyeljük az állapotokat

        System.out.println("t1 állapota: " + t1.getState()); // várhatóan TIMED_WAITING
        System.out.println("t2 állapota: " + t2.getState()); // várhatóan BLOCKED (nem fér hozzá a lockhoz)

        System.out.println("\n=== 2. Producer-Consumer modell ===");

        // Létrehozunk egy 5 elemű pufferrel rendelkező közös tárat
        SharedBuffer buffer = new SharedBuffer(5);
        List<Thread> workers = new ArrayList<>();

        // Indítunk 3 termelőt
        for (int i = 0; i < 3; i++) {
            workers.add(new Thread(new Producer("P" + i, buffer)));
        }

        // Indítunk 2 fogyasztót
        for (int i = 0; i < 2; i++) {
            workers.add(new Thread(new Consumer("C" + i, buffer)));
        }

        Collections.shuffle(workers); // véletlenszerű sorrendben indítjuk őket
        for (Thread t : workers) {
            t.start();
        }

        Thread.sleep(1000); // időt hagyunk a termelés-fogyasztás végbemenetelére

        System.out.println("\n=== 3. Olvasó–író szinkronizáció ===");

        // Dokumentum példány, amiben legfeljebb 2 olvasó lehet egyszerre
        Document doc = new Document(2);

        // Három író
        for (int i = 0; i < 3; i++) {
            new Thread(new Writer(doc, "W" + i)).start();
        }

        // Öt olvasó
        for (int i = 0; i < 5; i++) {
            new Thread(new Reader(doc, "R" + i)).start();
        }

        Thread.sleep(2000); // idő a műveleteknek

        System.out.println("\n=== 4. Evő filozófusok probléma ===");

        // Létrehozunk 5 villát (semaphore = 1)
        List<Semaphore> forks = new ArrayList<>();
        for (int i = 0; i < 5; i++) forks.add(new Semaphore(1));

        // Filozófus nevek
        String[] names = {"Sokrates", "Plato", "Aristotle", "Kant", "Nietzsche"};

        // Mindegyik filozófus kap egy bal és egy jobb villát (ciklikusan)
        for (int i = 0; i < 5; i++) {
            Semaphore left = forks.get(i);
            Semaphore right = forks.get((i + 1) % 5);
            new Thread(new Philosopher(names[i], left, right)).start();
        }

        Thread.sleep(3000); // idő az evésre és gondolkodásra

        System.out.println("=== Vége ===");
    }
}

// ----------- 1. példa: BLOCKED állapot demonstrálása -----------

class BlockingDemo extends Thread {
    private final Object lock;

    public BlockingDemo(Object lock) {
        this.lock = lock;
    }

    @Override
    public void run() {
        synchronized (lock) { // kritikus szakasz kezdete
            try {
                Thread.sleep(500); // zárolás után alvás
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

// ----------- 2. példa: Producer-Consumer modell -----------

class SharedBuffer {
    private final List<Integer> buffer = new ArrayList<>();
    private final int capacity;

    public SharedBuffer(int capacity) {
        this.capacity = capacity;
    }

    public synchronized void produce(String who) {
        while (buffer.size() == capacity) {
            System.out.println(who + " várakozik (tele van a puffer).");
            try {
                wait(); // megvárja, hogy legyen hely
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        int item = new Random().nextInt(100);
        buffer.add(item);
        System.out.println(who + " termelt: " + item + " → " + buffer);
        notifyAll(); // értesítjük a fogyasztókat
    }

    public synchronized void consume(String who) {
        while (buffer.isEmpty()) {
            System.out.println(who + " várakozik (üres a puffer).");
            try {
                wait(); // megvárja, hogy legyen elem
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        int item = buffer.remove(0);
        System.out.println(who + " kivett: " + item + " → " + buffer);
        notifyAll(); // értesítjük a termelőket
    }
}

class Producer implements Runnable {
    private final String name;
    private final SharedBuffer buffer;

    public Producer(String name, SharedBuffer buffer) {
        this.name = name;
        this.buffer = buffer;
    }

    public void run() {
        buffer.produce(name);
    }
}

class Consumer implements Runnable {
    private final String name;
    private final SharedBuffer buffer;

    public Consumer(String name, SharedBuffer buffer) {
        this.name = name;
        this.buffer = buffer;
    }

    public void run() {
        buffer.consume(name);
    }
}

// ----------- 3. példa: Olvasó–író szinkronizáció -----------

class Document {
    private final int maxReaders;
    private int activeReaders = 0;
    private boolean writing = false;

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition canWrite = lock.newCondition();
    private final Condition canRead = lock.newCondition();

    public Document(int maxReaders) {
        this.maxReaders = maxReaders;
    }

    public void write(String name) {
        lock.lock();
        try {
            while (activeReaders > 0 || writing) {
                canWrite.await(); // vár, amíg nem olvas és nem ír más
            }
            writing = true;
            System.out.println(name + " ír...");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

        simulateWork(); // szimulált írás

        lock.lock();
        try {
            writing = false;
            System.out.println(name + " befejezte az írást.");
            canRead.signalAll(); // olvasók ébresztése
            canWrite.signal();   // írók ébresztése
        } finally {
            lock.unlock();
        }
    }

    public void read(String name) {
        lock.lock();
        try {
            while (writing || activeReaders == maxReaders) {
                canRead.await(); // várunk, ha ír valaki, vagy elértük az olvasási limitet
            }
            activeReaders++;
            System.out.println(name + " olvas...");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

        simulateWork(); // szimulált olvasás

        lock.lock();
        try {
            activeReaders--;
            System.out.println(name + " befejezte az olvasást.");
            if (activeReaders == 0) canWrite.signal(); // ha senki sem olvas → író jöhet
            canRead.signalAll(); // több olvasó is jöhet
        } finally {
            lock.unlock();
        }
    }

    private void simulateWork() {
        try {
            Thread.sleep(200); // szimuláció
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class Reader implements Runnable {
    private final Document document;
    private final String name;

    public Reader(Document document, String name) {
        this.document = document;
        this.name = name;
    }

    public void run() {
        document.read(name);
    }
}

class Writer implements Runnable {
    private final Document document;
    private final String name;

    public Writer(Document document, String name) {
        this.document = document;
        this.name = name;
    }

    public void run() {
        document.write(name);
    }
}

// ----------- 4. példa: Evő filozófusok -----------

class Philosopher implements Runnable {
    private final String name;
    private final Semaphore leftFork;
    private final Semaphore rightFork;

    public Philosopher(String name, Semaphore left, Semaphore right) {
        this.name = name;
        this.leftFork = left;
        this.rightFork = right;
    }

    public void run() {
        for (int i = 0; i < 5; i++) {
            think(); // gondolkodik
            eat();   // próbál enni
        }
    }

    private void think() {
        System.out.println(name + " gondolkodik...");
        sleepRandom();
    }

    private void eat() {
        try {
            leftFork.acquire();  // próbálja felvenni a bal villát
            rightFork.acquire(); // próbálja felvenni a jobb villát
            System.out.println(name + " eszik.");
            sleepRandom();       // szimulált evés
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            leftFork.release();  // villa letétele
            rightFork.release();
            System.out.println(name + " befejezte az evést.");
        }
    }

    private void sleepRandom() {
        try {
            Thread.sleep(new Random().nextInt(300)); // véletlenszerű alvás
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

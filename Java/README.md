# Java - PP

## Alapfogalmak

### Szál (Thread)

- Egy végrehajtási szálat reprezentál.
- Létrehozható örökléssel (`extends Thread`) vagy interfészen keresztül (`implements Runnable`).
- A futtatást a `.start()` metódussal indítjuk, a `.run()` nem indít új szálat!

### Runnable interfész

- Funkcionális interfész: csak a `run()` metódust tartalmazza.
- Egy szál által futtatott feladat egységét írjuk meg benne.



## Szinkronizáció

### synchronized kulcsszó

- Kritikus szakaszok védelmére szolgál.
- Metódus vagy blokk szintjén is használható.

### wait() / notify() / notifyAll()

- Az `Object` osztály metódusai.
- Csak `synchronized` blokkon belül használhatóak.
- Feltételes várakozást és ébresztést valósítanak meg.



## Magasabb szintű vezérlők

### Semaphore (java.util.concurrent)

- Egyszerre több szálnak engedélyez hozzáférést.
- `acquire()` → lefoglal, `release()` → felszabadít.

### ReentrantLock

- Rugalmasabb zárolás, mint `synchronized`.
- Lehet fair módú is (`new ReentrantLock(true)`).
- Használható `tryLock()` és `lockInterruptibly()` is.

### Condition

- `await()`, `signal()`, `signalAll()` metódusokat ad a `Lock` objektumokhoz.
- Használata `lock.newCondition()` létrehozásával történik.



## Executor framework

### ExecutorService

- Szálkezelés elválasztása a feladatkezeléstől.
- Példák:
  - `Executors.newFixedThreadPool(n)`
  - `Executors.newCachedThreadPool()`

### Future és Callable

- A `Future` lehetőséget ad aszinkron eredmények lekérdezésére.
- A `Callable` interfész visszatérési értéket ad (`call()` metódus).



## További eszközök

| Eszköz              | Funkció                                 |
|---------------------|-----------------------------------------|
| `AtomicInteger`     | Atomi műveletek egész számokon          |
| `CountDownLatch`    | Szálak várakoztatása mások befejezésére |
| `CyclicBarrier`     | Szálak közös szinkronpontba érkezése    |
| `BlockingQueue`     | Termelő-fogyasztó minta támogatása      |



## Hibatípusok

- **Race condition**: Több szál versenyez egy erőforrásért, eredmény nem determinisztikus.
- **Deadlock**: Két vagy több szál egymástól várja a felszabadulást.
- **Livelock**: Szálak aktívan dolgoznak, de mégsem haladnak előre.



## Gyakori minták

### Producer–Consumer

- Használható: `BlockingQueue`, `Semaphore`, `wait/notify`

### Dining Philosophers

- Klasszikus szinkronizációs probléma.
- Jó példa holtpont kezelésére.

### Thread Pool

- Feladatok dinamikus kiosztása a háttérben lévő fix számú szálra.
- `ExecutorService` segítségével.



## Kapcsolódó linkek

- [Java Concurrency API](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/package-summary.html)
- [Java Tutorials - Concurrency](https://docs.oracle.com/javase/tutorial/essential/concurrency/)
- [Java Memory Model](https://www.cs.umd.edu/~pugh/java/memoryModel/)

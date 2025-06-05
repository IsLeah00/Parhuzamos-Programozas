import java.util.concurrent.locks.*;

class Kapu {
    private final int MAX = 9;
    private int foglalt = 0;

    private final Lock zart = new ReentrantLock();
    private final Condition szabad = zart.newCondition();

    public void belep(int szelesseg, String nev) {
        zart.lock();
        try {
            while (foglalt + szelesseg > MAX) {
                szabad.await();
            }
            foglalt += szelesseg;
            System.out.println(nev + " belépett (" + szelesseg + ")");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            zart.unlock();
        }
    }

    public void setal(String nev) {
        System.out.println(nev + " sétál a kapuban...");
    }

    public void kilep(int szelesseg, String nev) {
        zart.lock();
        try {
            foglalt -= szelesseg;
            System.out.println(nev + " kilépett (" + szelesseg + ")");
            szabad.signalAll(); // lehet, hogy több szál is vár
        } finally {
            zart.unlock();
        }
    }
}

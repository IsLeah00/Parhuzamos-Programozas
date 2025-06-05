public class Ember extends Thread {
    private final Kapu kapu;
    private final int szelesseg;

    public Ember(Kapu kapu, int szelesseg, String nev) {
        super(nev);
        this.kapu = kapu;
        this.szelesseg = szelesseg;
    }

    @Override
    public void run() {
        kapu.belep(szelesseg, getName());
        kapu.setal(getName());
        kapu.kilep(szelesseg, getName());
    }
}

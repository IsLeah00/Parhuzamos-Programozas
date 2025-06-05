public class Main {
    public static void main(String[] args) {
        Kapu kapu = new Kapu();

        for (char c = 'A'; c <= 'Z'; c++) {
            int szelesseg = (Math.random() < 0.5) ? 1 : 2;
            new Ember(kapu, szelesseg, String.valueOf(c)).start();
        }
    }
}

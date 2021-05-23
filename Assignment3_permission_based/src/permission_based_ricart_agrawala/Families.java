package permission_based_ricart_agrawala;

public class Families {
    public Family[] families = new Family[Main.number_of_families];
    public int count = 0;       // count how many families have finish all shopping.

    public Family[] getFamilies() {
        return families;
    }

    public void init() {
        for (int i = 0; i < Main.number_of_families; i ++) {
            families[i] = new Family(i);
        }
    }
    public void start() {
        for (int i = 0; i < Main.number_of_families; i ++) {
            families[i].start();
        }
    }
}

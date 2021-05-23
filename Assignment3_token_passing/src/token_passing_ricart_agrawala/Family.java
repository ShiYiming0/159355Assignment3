package token_passing_ricart_agrawala;

import java.util.ArrayList;

public class Family extends Thread{

    int myID;
    boolean haveToken = false;
    ArrayList<Integer> requested = new ArrayList<>();
    ArrayList<Integer> granted = new ArrayList<>();
    int myNum = 0;
    boolean inCS = false;
    Communicator communicator = new Communicator();
    int shopping_times = 3;     // each family shopping 3 times
    java.util.Random r = new java.util.Random();

    Family(int id) {
        this.myID = id;
        if (this.myID == 0) { haveToken = true; }
        for (int i = 0; i < Main.number_of_families; i ++) {
            requested.add(0);
            granted.add(0);
        }
    }

    public void sendToken() throws InterruptedException {
        // send token to next valid family
        for (int i = myID + 1; i < Main.number_of_families; i ++) {
            if (requested.get(i) > granted.get(i)) {
                for (Family n : Main.families.getFamilies()) {
                    if (n.myID == i) {
                        communicator.send(2, n, myID, -1);
                        haveToken = false;
                        return;
                    }
                }
            }
        }
        //if its the final family, start from the first family
        for (int i = 0; i < myID; i ++) {
            if (requested.get(i) > granted.get(i)) {
                for (Family n : Main.families.getFamilies()) {
                    if (n.myID == i) {
                        communicator.send(2, n, myID, -1);
                        haveToken = false;
                        return;
                    }
                }
            }
        }
    }

    public void run() {
        do {
            try {
                Thread.sleep((long) (Math.random() * 100));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // receive message.
            try {
                Integer[] message = communicator.recieve();
                if (message != null) {
                    int request_or_token = message[0];
                    int source = message[1];
                    int requestedNum = message[2];
                    if (request_or_token == 1) {        // deal with request
                        requested.set(source, Math.max(requested.get(source), requestedNum));
                        if (haveToken && !inCS) { sendToken(); }
                    }       // get token
                    else if (request_or_token == 2) {
                        haveToken = true;
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (shopping_times > 0 && !inCS && r.nextInt(50) == 1) {
                myNum ++;
                for (Family n : Main.families.getFamilies()) {
                    if (n.myID != myID) {
                        try {
                            communicator.send(1, n, myID, myNum);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                inCS = true;
            }
            if (haveToken && inCS) {
                // critical section
                System.out.println("---------------------------------------");
                System.out.println("Family " + myID + " is going to market.");
                System.out.println("Shopping...");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Family " + myID + " is going home.");
                shopping_times--;
                if (shopping_times == 0) {
                    Main.families.count++;
                    System.out.println(Main.families.count + " families have done all shopping");
                }
                // end critical section
                for (Family n : Main.families.getFamilies()) { n.granted.set(myID, myNum); }
                inCS = false;
                try {
                    sendToken();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } while (Main.families.count < Main.number_of_families);
    }

}

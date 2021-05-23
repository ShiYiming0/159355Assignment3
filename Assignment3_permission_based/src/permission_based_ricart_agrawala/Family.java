package permission_based_ricart_agrawala;

import java.util.ArrayList;

public class Family extends Thread {

    int myID;
    int myNum = 0;
    ArrayList<Integer> deferred = new ArrayList<>();
    int highestNum = 0;
    boolean requestCS = false;
    java.util.Random r = new java.util.Random();        // used to randomly request critical section.
    Communicator communicator = new Communicator();     // each family has a LinkedBlockingQueue for communication.
    ArrayList<Integer> recieveList = new ArrayList<>(); // wait these families' reply.
    int shopping_times = 3;     // each family shopping 3 times

    Family (int id) {
        this.myID = id;
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
                if (message != null) {  // the LinkedBlockingQueue is empty.
                    int request_or_reply = message[0];
                    int source = message[1];
                    int requestedNum = message[2];
                    if (request_or_reply == 1) {        // deal with request.
                        highestNum = Math.max(highestNum, requestedNum);
                        if (!requestCS || requestedNum < myNum) {
                            for (Family n : Main.families.getFamilies()) {
                                if (n.myID == source) {
                                    communicator.send(2, n, myID, -1);
                                }
                            }
                        } else if (requestedNum == myNum) {
                            // when they have same num, let family with larger id go to market first.
                            if (source > myID) {
                                for (Family n : Main.families.getFamilies()) {
                                    if (n.myID == source) {
                                        communicator.send(2, n, myID, -1);
                                    }
                                }
                            } else {
                                deferred.add(source);
                            }
                        } else {
                            deferred.add(source);
                        }
                    }       // deal with reply
                    else if (request_or_reply == 2) {
                        recieveList.remove(0);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (shopping_times > 0 && !requestCS && r.nextInt(10) == 1) {
                requestCS = true;
                myNum = highestNum + 1;
                // send request to other families.
                for (Family n : Main.families.getFamilies()) {
                    if (n.myID != myID) {
                        try {
                            communicator.send(1, n, myID, myNum);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                recieveList = new ArrayList<>();
                for (int i = 0; i < Main.number_of_families; i++) {
                    recieveList.add(i);
                }
                recieveList.remove(myID);
            }
            if (requestCS) {
                if (recieveList.size() == 0) {
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
                    requestCS = false;
                    for (int f : deferred) {
                        try {       // reply to deferred families.
                            for (Family n : Main.families.getFamilies()) {
                                if (n.myID == f) {
                                    communicator.send(2, n, myID, -1);
                                }
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    deferred = new ArrayList<Integer>();
                }
            }
        } while (Main.families.count < Main.number_of_families);
    }
}



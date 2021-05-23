package permission_based_ricart_agrawala;

import java.util.concurrent.LinkedBlockingQueue;

public class Communicator extends LinkedBlockingQueue<Integer[]> {

                                // request_or_reply: 1=request, 2=reply.
    public void send(int request_or_reply, Family n, int my_id, int my_num) throws InterruptedException {
        Integer[] message = {request_or_reply, my_id, my_num};
        n.communicator.offer(message);
//        if (request_or_reply == 1) { System.out.println(my_id + " request to " + n.myID + " with num " + my_num); }
//        else { System.out.println(my_id + " reply to " + n.myID); }
    }

    public Integer[] recieve() throws InterruptedException {
        return this.poll();
    }
}

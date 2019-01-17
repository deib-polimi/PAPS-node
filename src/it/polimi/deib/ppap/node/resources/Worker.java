package it.polimi.deib.ppap.node.resources;

import it.polimi.deib.ppap.node.services.ServiceRequest;

import java.util.concurrent.TimeUnit;

public class Worker extends Thread {

    private DynamicThreadPool pool;
    private boolean available;
    private String id;
    private boolean alive;

    public Worker(String id, DynamicThreadPool pool){
        this.pool = pool;
        this.id = id;
        available = false;
        alive = true;
    }

    protected synchronized void setAvailable(boolean available){
        this.available = available;
    }

    protected synchronized void kill(){
        alive = false;
    }

    @Override
    public void run() {
        super.run();

        while (alive || available){
            boolean _available = false;

            synchronized (this) {
                if (available) {
                    _available = true;
                }
            }

            try {
                if (_available){
                    ServiceRequest request = pool.queue.poll(500, TimeUnit.MILLISECONDS);
                    if(request == null) {
                        if (!alive)
                            available = false;
                        continue;
                    }
                    request.run();
                    request.setEnd();
                    pool.threadEnded(request);
                }
                else {
                    System.out.println("Waiting...");
                    synchronized (this) {
                        wait();
                    }
                    System.out.println("notified...");
                }
            } catch (InterruptedException e) {
                        e.printStackTrace();
            }
        }

    }


}

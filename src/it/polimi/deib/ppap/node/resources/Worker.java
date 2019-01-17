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
            try {
                if (available){
                    Runnable task = pool.queue.poll(500, TimeUnit.MILLISECONDS);
                    if(task == null) {
                        if (!alive)
                            available = false;
                        continue;
                    }
                    pool.taskStarted(task);
                    task.run();
                    pool.taskExecuted(task);
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

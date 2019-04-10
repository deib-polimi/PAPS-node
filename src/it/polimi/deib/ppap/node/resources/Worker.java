package it.polimi.deib.ppap.node.resources;

import it.polimi.deib.ppap.node.services.ServiceRequest;

import java.util.concurrent.TimeUnit;

public class Worker extends Thread {

    private DynamicThreadPool pool;
    private String id;
    private boolean kill;

    public Worker(String id, DynamicThreadPool pool){
        this.pool = pool;
        this.id = id;
        kill = false;
    }


    protected synchronized void kill(){
        kill = true;
    }

    @Override
    public void run() {
        super.run();

        while (!kill){
            try {
                Runnable task = pool.queue.poll(500, TimeUnit.MILLISECONDS);
                if(task != null ){
                    pool.taskStarted(task);
                    task.run();
                    pool.taskExecuted(task);
                }

            } catch (Exception e) {
                kill = true;
            }
        }
    }

}

package it.polimi.deib.ppap.node.resources;

import it.polimi.deib.ppap.node.services.ServiceRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

import java.util.concurrent.LinkedBlockingQueue;

public class DynamicThreadPool implements Executor, TaskListener {

    private Optional<TaskListener> listener;
    protected LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
    private List<Worker> workers = new ArrayList<>();
    private int size;
    private final int maxSize;

    public DynamicThreadPool(int startSize, int maxSize){
        this.maxSize = maxSize;
        this.listener = Optional.empty();
        for (int i = 1; i <= maxSize; i++){
            Worker worker = new Worker(String.valueOf(i), this);
            workers.add(worker);
        }

        setSize(startSize);
    }

    public void setTaskListener(TaskListener listener){
        this.listener = Optional.of(listener);
    }

    public void start(){
        for(Worker w: workers)
            w.start();
    }

    public void shutdownNow(){
        for(Worker w: workers) {
            w.setAvailable(false);
        }
        shutdown();
    }

    public void shutdown(){
        for(Worker w: workers) {
            w.kill();
            synchronized (w) {
                w.notifyAll();
                try {
                    w.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public synchronized void setSize(int size){
        if (size > maxSize)
            size = maxSize;

        this.size = size;

        for (int i = 0; i < maxSize; i++){
            Worker worker = workers.get(i);
            synchronized (worker){
                worker.setAvailable(i < size);
                worker.notify();
            }
        }
    }

    public synchronized int getSize(){
        return size;
    }

    public synchronized void execute(Runnable task){
        queue.offer(task);
    }

    @Override
    public void taskStarted(Runnable task) {
        listener.ifPresent((l)-> l.taskStarted(task));
    }

    public synchronized void taskExecuted(final Runnable task) {
        listener.ifPresent((l)-> l.taskExecuted(task));
    }


}

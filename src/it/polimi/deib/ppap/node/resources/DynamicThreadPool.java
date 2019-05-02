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
    private final int startSize;

    public DynamicThreadPool(int startSize, int maxSize){
        this.maxSize = maxSize;
        this.listener = Optional.empty();
        this.startSize = 2;
        this.size = 0;
    }

    public void setTaskListener(TaskListener listener){
        this.listener = Optional.of(listener);
    }

    public void start(){
        setSize(startSize);
    }

    public void shutdownNow(){
        for(Worker w: workers) {
            shutdownNow(w);
        }
    }

    private void shutdownNow(Worker w) {
        w.kill();
        /*
        synchronized (w) {
            w.notifyAll();
            try {
                w.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }*/
    }

    public synchronized void setSize(int size){
        if (size > maxSize)
            size = maxSize;

        var oldSize = this.size;
        this.size = size;

        var diff = size - workers.size();
        if (diff >= 0) {
            for (var i = 0; i < diff; i++) {
                var worker = new Worker(String.valueOf(oldSize+i+1),this);
                workers.add(worker);
                worker.start();
            }
        }
        else {
            diff = -diff;
            for (var i = 0; i < diff; i++) {
                shutdownNow(workers.remove(0));
            }
        }

        this.size = size;
        assert workers.size() == size;
    }

    public synchronized int getSize(){
        return size;
    }

    public synchronized void execute(Runnable task){
        this.taskEnqueued(task);
        queue.offer(task);
    }

    @Override
    public void taskEnqueued(Runnable task) {
        listener.ifPresent((l)-> l.taskEnqueued(task));
    }

    @Override
    public void taskStarted(Runnable task) {
        listener.ifPresent((l)-> l.taskStarted(task));
    }

    public synchronized void taskExecuted(final Runnable task) {
        listener.ifPresent((l)-> l.taskExecuted(task));
    }


}

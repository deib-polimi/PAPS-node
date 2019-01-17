package it.polimi.deib.ppap.node.resources;

import it.polimi.deib.ppap.node.services.ServiceRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class DynamicThreadPool  {

    private ExecutorService printExecutor = Executors.newSingleThreadExecutor();
    protected LinkedBlockingQueue<ServiceRequest> queue = new LinkedBlockingQueue<>();
    private List<Worker> workers = new ArrayList<>();
    private int size;
    private final int maxSize;

    public DynamicThreadPool(int startSize, int maxSize){
        this.maxSize = maxSize;

        for (int i = 1; i <= maxSize; i++){
            Worker worker = new Worker(String.valueOf(i), this);
            workers.add(worker);
        }

        setSize(startSize);
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
                w.notify();
                try {
                    w.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        printExecutor.shutdown();


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

    public synchronized void execute(ServiceRequest request){
        request.setStart();
        queue.offer(request);
    }


    public synchronized void threadEnded(final ServiceRequest request) {
        printExecutor.execute(() -> System.out.println("Executed request "+request.getName()+
                " in "+request.getResponseTime()+" queue "+request.getQueueTime()));
    }

    public static void main(String[] args) throws InterruptedException {

        DynamicThreadPool pool = new DynamicThreadPool(16, 16);

        pool.start();

        for (int i = 1; i <= 16*20; i++){
            pool.execute(new ServiceRequest(String.valueOf(i), 50));
        }

        pool.shutdown();
    }

}

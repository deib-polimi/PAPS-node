package it.polimi.deib.ppap.node.services;

import it.polimi.deib.ppap.node.resources.DynamicThreadPool;
import it.polimi.deib.ppap.node.resources.TaskListener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServiceExecutor implements TaskListener<ServiceRequest>  {

    private DynamicThreadPool pool;
    private ExecutorService printExecutor = Executors.newSingleThreadExecutor();

    public ServiceExecutor(int poolSize, int poolMaxSize){
        pool = new DynamicThreadPool(poolSize, poolMaxSize);
        pool.setTaskListener(this);
    }

    public void start(){
        pool.start();
    }

    public void shutdown(){
        pool.shutdown();
        printExecutor.shutdown();
    }

    public void shutdownNow(){
        pool.shutdownNow();
        printExecutor.shutdown();
    }

    public void execute(ServiceRequest request) {
        request.setStart();
        pool.execute(request);
    }

    @Override
    public void taskStarted(ServiceRequest task) {
        task.setStartService();
    }

    @Override
    public void taskExecuted(ServiceRequest task) {
        task.setEnd();
        printExecutor.execute(() -> System.out.println("Executed request "+task.getName()+
                " in "+task.getResponseTime()+" queue "+task.getQueueTime()));
    }

    public static void main(String[] args) throws InterruptedException {

        ServiceExecutor service = new ServiceExecutor(16, 16);

        service.start();

        for (int i = 1; i <= 16*20; i++){
            service.execute(new ServiceRequest(String.valueOf(i), 50));
        }

        service.shutdown();
    }
}

package it.polimi.deib.ppap.node.services;

import it.polimi.deib.ppap.node.NodeFacade;
import it.polimi.deib.ppap.node.resources.DynamicThreadPool;
import it.polimi.deib.ppap.node.resources.TaskListener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class ServiceExecutor implements TaskListener<ServiceRequest>  {

    private DynamicThreadPool pool;
    private ExecutorService callbackExecutor = Executors.newSingleThreadExecutor();

    private TaskListener listener;

    public ServiceExecutor(Service service, long nodeMemory, TaskListener listener){
        int max = (int) Math.floor(nodeMemory/service.getMemory());
        pool = new DynamicThreadPool((int) service.getTargetAllocation(), max);
        pool.setTaskListener(this);
        this.listener = listener;
    }

    public void start(){
        pool.start();
    }

    public void shutdown(){
        pool.shutdown();
        callbackExecutor.shutdown();

    }

    public void shutdownNow(){
        pool.shutdownNow();
        callbackExecutor.shutdownNow();
    }

    public void execute(ServiceRequest request) {
        request.setStart();
        pool.execute(request);
    }

    public void setSize(int size){
        pool.setSize(size);
    }

    @Override
    public void taskStarted(ServiceRequest task) {
        task.setStartService();
        callbackExecutor.execute(() -> {
            listener.taskStarted(task);
        });

    }

    @Override
    public void taskExecuted(ServiceRequest task) {
        task.setEnd();
        callbackExecutor.execute(() -> {
            listener.taskExecuted(task);
        });        //printExecutor.execute(() -> System.out.println("Executed request for service "+task.getService().getId()+
               // " in "+task.getResponseTime()+" queue "+task.getQueueTime()+ " service "+task.getServiceTime()));
    }




    /*
    public static void main(String[] args)  {

        ServiceExecutor service = new ServiceExecutor(newService, 16);

        service.start();
        long start = System.currentTimeMillis();

        for (int i = 1; i <= 16*20; i++){
            //service.execute(new ServiceRequest(String.valueOf(i), 50));
        }

        System.out.println("exec: "+ (System.currentTimeMillis() - start));

        service.shutdown();
    }*/
}

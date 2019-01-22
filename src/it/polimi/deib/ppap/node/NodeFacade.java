package it.polimi.deib.ppap.node;


import it.polimi.deib.ppap.node.control.PlannerController;
import it.polimi.deib.ppap.node.history.HistoryData;
import it.polimi.deib.ppap.node.history.History;
import it.polimi.deib.ppap.node.monitoring.Monitor;
import it.polimi.deib.ppap.node.monitoring.MonitoringData;
import it.polimi.deib.ppap.node.services.Service;
import it.polimi.deib.ppap.node.services.ServiceExecutor;
import it.polimi.deib.ppap.node.services.ServiceRequest;

import java.util.*;


public class NodeFacade {

    private long memory;
    private long controlPeriod;

    private Map<Service, ServiceExecutor> services = new HashMap<>();
    private PlannerController controller;
    private Monitor monitor = new Monitor();
    private Timer timer = new Timer();
    private History history = new History();

    public NodeFacade(long memory, long controlPeriodMillis, float alpha){
        this.memory = memory;
        this.controlPeriod = controlPeriodMillis;
        controller = new PlannerController(alpha, memory);
    }

    public void start(){
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                tick();
            }
        }, controlPeriod, controlPeriod);
    }

    public synchronized void addService(Service service){
        ServiceExecutor serviceExecutor = new ServiceExecutor(service, memory, monitor);
        this.services.put(service, serviceExecutor);
        controller.addService(service);
        this.monitor.addService(service);
        this.history.addService(service);
        serviceExecutor.start();
    }

    public synchronized void removeService(Service service){
        ServiceExecutor executor = services.get(service);
        executor.shutdownNow();
        controller.removeService(service);
        monitor.removeService(service);
        this.history.removeService(service);
        services.remove(service);
    }

    public void execute(ServiceRequest request) {
        if (services.containsKey(request.getService()))
            this.services.get(request.getService()).execute(request);
        else throw new ServiceNotFoundException();
    }

    private void tick(){
        Map<Service, MonitoringData> monitoring = monitor.read();
        System.out.println(monitoring);
        Map<Service, Float> allocations = controller.control(monitoring);
        System.out.println(allocations);
        allocations.forEach((service, allocation) -> services.get(service).setSize(allocation.intValue()));
        allocations.forEach((service, allocation) -> history.addData(service, monitoring.get(service), allocation, getLastOptimalAllocation(service)));

    }

    // retrieve and clear history of a service
    public List<HistoryData> getHistory(Service service){
        return history.read(service);
    }

    public float getLastOptimalAllocation(Service service){
        return controller.getLastOptimalAllocation(service);
    }


    public class ServiceNotFoundException extends RuntimeException { }



    public static void main(String[] args){

        NodeFacade facade = new NodeFacade(8192, 10*1000, 0.5f);
        Service one = new Service("1", 128, 120);
        Service two = new Service("2", 256, 100);
        one.setTargetAllocation(1);
        two.setTargetAllocation(1);
        facade.addService(one);
        //facade.addService(two);
        facade.start();

        new Thread(executeRequests(facade, 100000, one)).start();
       // new Thread(executeRequests(facade, 100000, two)).start();

    }

    private static Runnable executeRequests(NodeFacade facade, long num, Service service){
        return () -> {
                for (int i = 0; i < num; i++) {
                    facade.execute(new ServiceRequest(service, (long) service.getSLA()-3));
                    try {
                        Thread.sleep(Math.max(120-i/120, 70));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
    }




}

package it.polimi.deib.ppap.node;


import it.polimi.deib.ppap.node.commons.Utils;
import it.polimi.deib.ppap.node.control.PlannerController;
import it.polimi.deib.ppap.node.history.HistoryData;
import it.polimi.deib.ppap.node.history.History;
import it.polimi.deib.ppap.node.monitoring.Monitor;
import it.polimi.deib.ppap.node.monitoring.MonitoringData;
import it.polimi.deib.ppap.node.services.Service;
import it.polimi.deib.ppap.node.services.ServiceExecutor;
import it.polimi.deib.ppap.node.services.ServiceRequest;
import it.polimi.deib.ppap.node.commons.NormalDistribution;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.*;


public class NodeFacade {

    private long memory;
    private long controlPeriod;

    private Map<Service, ServiceExecutor> services = new HashMap<>();
    private PlannerController controller;
    private Monitor monitor = new Monitor();
    private Timer timer = new Timer();
    private History history = new History();
    private ExecutorService loggerService = Executors.newSingleThreadExecutor();
    private Logger logger;

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

    public void setLogger(Logger logger){
        this.logger = logger;
        logger.info("ts,id,rt,rq,al");
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

    public void stop(){
        timer.cancel();
        for(Service s : new HashSet<>(services.keySet())){
            removeService(s);
        }
        loggerService.shutdownNow();
    }

    public void execute(ServiceRequest request) {
        if (services.containsKey(request.getService()))
            this.services.get(request.getService()).execute(request);
        else throw new ServiceNotFoundException();
    }

    private void tick(){
        Map<Service, MonitoringData> monitoring = monitor.read();
        Map<Service, Float> allocations = controller.control(monitoring);
        allocations.forEach((service, allocation) -> services.get(service).setSize(allocation.intValue()));
        allocations.forEach((service, allocation) -> history.addData(service, monitoring.get(service), allocation, getLastOptimalAllocation(service)));
        long ts = System.currentTimeMillis();
        loggerService.execute( () -> {
            if (logger != null){
                for (Service s : monitoring.keySet()){
                    MonitoringData data = monitoring.get(s);
                    logger.info(ts+","+s+","+data.getResponseTime()+","+data.getRequests()+","+allocations.get(s));
                }
            }
        });
    }

    // retrieve and clear history of a service
    public List<HistoryData> getHistory(Service service){
        return history.read(service);
    }

    public float getLastOptimalAllocation(Service service){
        return controller.getLastOptimalAllocation(service);
    }

    public class ServiceNotFoundException extends RuntimeException { }


    public static void main(String[] args) throws InterruptedException, IOException {

        NodeFacade facade = new NodeFacade(8192, 3000, 0.9f);
        facade.setLogger(Utils.getLogger("exp1.log"));

        Service one = new Service("1", 128, 120);
        Service two = new Service("2", 256, 100);
        one.setTargetAllocation(32);
        two.setTargetAllocation(16);
        facade.addService(one);
        facade.addService(two);
        facade.start();

        Thread t1 = new Thread(executeRequests(facade, 1000, one));
        Thread t2 = new Thread(executeRequests(facade, 1000, two));

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        facade.stop();
        System.out.println("END");

    }


    private static Runnable executeRequests(NodeFacade facade, long num, Service service){
        return () -> {

            NormalDistribution n = Utils.getNormalDistribution(service.getSLA()*0.8, service.getSLA()*0.8*0.1);

            System.out.println("PHASE 1: "+service);
            // stable system at the beginning
            for (int i = 0; i < 200; i++) {
                facade.execute(new ServiceRequest(service, (long) n.random()));
                try {
                    Thread.sleep((long) service.getSLA());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("PHASE 2: "+service);
            // decreasing inter-arrival rate
            for (int i = 0; i < num/3; i++) {
                    facade.execute(new ServiceRequest(service, (long) n.random()));
                    try {
                        Thread.sleep((long) (n.random()*0.8));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            System.out.println("PHASE 3: "+service);
            // peak inter-arrival rate
            for (int i = 0; i < num/3; i++) {
                facade.execute(new ServiceRequest(service, (long) n.random()));
                try {
                    Thread.sleep((long) (n.random()*0.3));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("PHASE 4: "+service);
            // decreasing inter-arrival rate
            for (int i = 0; i < num/3; i++) {
                facade.execute(new ServiceRequest(service, (long) n.random()));
                try {
                    Thread.sleep((long) (n.random()*0.7));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("PHASE 5: "+service);
            // peak inter-arrival rate
            for (int i = 0; i < num; i++) {
                facade.execute(new ServiceRequest(service, (long) n.random()));
                try {
                    Thread.sleep((long) (n.random()*0.1));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


            System.out.println("END: "+service);

            };




    }




}

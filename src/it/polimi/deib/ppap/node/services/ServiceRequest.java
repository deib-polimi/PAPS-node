package it.polimi.deib.ppap.node.services;

public class ServiceRequest implements Runnable {

    private long start;
    private long startService;
    private long end;
    private String name;
    private long serviceAndNetworkTime;

    public ServiceRequest(String name, long serviceAndNetworkTime){
        this.name = name;
        this.serviceAndNetworkTime = serviceAndNetworkTime;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(serviceAndNetworkTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void setStart(){
        start = System.currentTimeMillis();
    }

    public void setStartService(){
        startService = System.currentTimeMillis();
    }


    public void setEnd(){
        end = System.currentTimeMillis();
    }

    public long getNominalServiceTime(){
        return serviceAndNetworkTime;
    }

    public long getServiceTime(){
        return end-startService;
    }

    public long getResponseTime(){
        return end-start;
    }

    public long getQueueTime(){
        return startService-start;
    }

    public String getName(){
        return name;
    }
}

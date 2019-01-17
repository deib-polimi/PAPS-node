package it.polimi.deib.ppap.node.services;

public class ServiceRequest implements Runnable {

    private long start;
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

    public void setEnd(){
        end = System.currentTimeMillis();
    }

    public long getResponseTime(){
        return end-start;
    }

    public long getQueueTime(){
        return getResponseTime()-serviceAndNetworkTime;
    }

    public String getName(){
        return name;
    }
}

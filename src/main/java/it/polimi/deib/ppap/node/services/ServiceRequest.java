package it.polimi.deib.ppap.node.services;

public class ServiceRequest implements Runnable {

    private long start;
    private long startService;
    private long end;
    private Service service;
    private long serviceTime;
    private long networkTime;

    public ServiceRequest(Service service, long serviceTime){
        this.service = service;
        this.serviceTime = serviceTime;
        this.networkTime = 0L;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(serviceTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setNetworkTime(long networkTime) {
        this.networkTime = networkTime;
    }

    public long getNetworkTime(){
        return networkTime;
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
        return serviceTime;
    }

    public long getServiceTime(){
        return end-startService;
    }

    public long getResponseTime(){
        return end-start+networkTime;
    }

    public long getQueueTime(){
        return startService-start;
    }

    public Service getService(){
        return service;
    }
}

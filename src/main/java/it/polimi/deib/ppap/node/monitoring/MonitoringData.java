package it.polimi.deib.ppap.node.monitoring;

public class MonitoringData {

    private long responseTime;
    private long requests;

    public MonitoringData(long responseTime, long requests){
        this.responseTime = responseTime;
        this.requests = requests;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public long getRequests() {
        return requests;
    }

    @Override
    public String toString() {
        return "rt: "+responseTime+" rq: "+requests;
    }
}

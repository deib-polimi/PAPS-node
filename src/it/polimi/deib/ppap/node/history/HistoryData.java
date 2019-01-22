package it.polimi.deib.ppap.node.history;

import it.polimi.deib.ppap.node.monitoring.MonitoringData;

public class HistoryData {

    private long requests;
    private long responseTime;
    private float allocation;
    private float optimalAllocation;
    private long timestamp;

    protected HistoryData(MonitoringData data, float allocation, float optimalAllocation){
        timestamp = System.currentTimeMillis();
        this.allocation = allocation;
        this.optimalAllocation = optimalAllocation;
        this.requests = data.getRequests();
        this.responseTime = data.getResponseTime();
    }

    public long getRequests() {
        return requests;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public float getAllocation() {
        return allocation;
    }

    public float getOptimalAllocation() {
        return optimalAllocation;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
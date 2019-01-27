package it.polimi.deib.ppap.node.monitoring;

import it.polimi.deib.ppap.node.resources.TaskListener;
import it.polimi.deib.ppap.node.services.Service;
import it.polimi.deib.ppap.node.services.ServiceExecutor;
import it.polimi.deib.ppap.node.services.ServiceRequest;
import javafx.concurrent.Task;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Monitor implements TaskListener<ServiceRequest> {

    public Map<Service, DataHolder> data = new HashMap<>();

    public void addService(Service service){
        this.data.put(service, new DataHolder());

    }

    public void removeService(Service service){
        this.data.remove(service);
    }

    public Map<Service, MonitoringData> read(){
        return data.entrySet().stream().map((e) -> new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue().read())).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
    }


    @Override
    public void taskEnqueued(ServiceRequest request) {
        if (data.containsKey(request.getService()))
            data.get(request.getService()).addArrival(1);
    }

    @Override
    public void taskStarted(ServiceRequest request) {

    }

    @Override
    public void taskExecuted(ServiceRequest request) {
        if (data.containsKey(request.getService())) {
            data.get(request.getService()).addRT(request.getResponseTime());
            data.get(request.getService()).addLeft(1);
        }
    }


    private class DataHolder {

        private long rt = 0;
        private long rtCount = 0;
        private long arrival = 0;
        private long left = 0;

        synchronized void addRT(long value) {
            rt += value;
            rtCount++;
        }

        synchronized void addArrival(long value) {
            this.arrival += value;
        }

        synchronized void addLeft(long value) {
            this.left += value;
        }

        synchronized MonitoringData read(){
            MonitoringData res = null;

            if (rtCount == 0 || arrival == 0) {
                res = new MonitoringData(0, 0);
            }
            else {
                res = new MonitoringData(rt/rtCount, arrival);
            }

            reset();

            return res;
        }

        private void reset(){
            rt = 0;
            rtCount = 0;
            arrival -= left;
            left = 0;
        }

    }
}

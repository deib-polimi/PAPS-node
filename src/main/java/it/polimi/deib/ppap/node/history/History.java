package it.polimi.deib.ppap.node.history;

import it.polimi.deib.ppap.node.monitoring.MonitoringData;
import it.polimi.deib.ppap.node.services.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class History {

    private Map<Service, List<HistoryData>> history = new HashMap<>();

    public void addService(Service service){
        history.put(service, new ArrayList<>());
    }

    public void removeService(Service service){
        history.remove(service);
    }

    public void addData(Service service, MonitoringData data, float allocation, float optimalAllocation){
        if (history.containsKey(service))
            history.get(service).add(new HistoryData(data, allocation, optimalAllocation));
    }

    public List<HistoryData> read(Service service){
        if (history.containsKey(service)) {
            List<HistoryData> res = new ArrayList<>(history.get(service));
            history.get(service).clear();
            return res;
        }
        return new ArrayList<>();
    }

}
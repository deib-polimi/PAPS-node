package it.polimi.deib.ppap.node.control;


import it.polimi.deib.ppap.node.monitoring.MonitoringData;
import it.polimi.deib.ppap.node.services.Service;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PlannerController {

    private float alpha;
    private long nodeMemory;
    private Map<Service, Planner> planners = new HashMap<>();

    public PlannerController(float alpha, long nodeMemory){
        this.alpha = alpha;
        this.nodeMemory = nodeMemory;
    }

    public synchronized Map<Service, Float> control(Map<Service, MonitoringData> monitoring, boolean control) {
        if (control) {
        Map<Service, Float> allocations = monitoring.entrySet().stream()
                .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), planners.get(e.getKey()).nextResourceAllocation(e.getValue())))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

            allocations = solveContention(mapAllocations(allocations, Math::ceil));
            allocations.entrySet().forEach(e -> {
                        planners.get(e.getKey()).updateState(e.getValue());
                        System.out.println("Allocated " + e.getValue() + " CTN(s) for " + e.getKey());
                    }
            );
            return allocations;
        }
        else {
            return planners.keySet().stream()
                    .map(e -> new AbstractMap.SimpleEntry<>(e, e.getTargetAllocation()))
                    .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
        }
    }

    public float getLastOptimalAllocation(Service service){
        return planners.get(service).lastOptimalAllocation();
    }


    private <E> Map<Service, Float> mapAllocations(Map<Service, Float> allocations, Function<Float, Double> f){
        return allocations.entrySet().stream()
                .map((e) -> new AbstractMap.SimpleEntry<>(e.getKey(), f.apply(e.getValue()).floatValue())).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
    }

    private Map<Service, Float> solveContention(Map<Service, Float> allocations){
        float sum = allocations.entrySet().stream().reduce(0F, (acc, e) -> acc + e.getKey().getMemory() * e.getValue(), (a, b) -> a + b);

        if (sum <= nodeMemory){
            return allocations;
        }

        return allocations.entrySet().stream().map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), heuristic(e.getKey(), e.getValue(), sum))).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
    }

    public synchronized void addService(Service service){
        planners.put(service, new Planner(service, alpha));
    }

    public synchronized void removeService(Service service){
        planners.remove(service);
    }

    private float heuristic(Service service, float request, float allocationsSum){
        float weight =  service.getMemory()*(request/allocationsSum + service.getTargetAllocation()/nodeMemory)/2;
        return (float) Math.floor(nodeMemory*weight/service.getMemory());
    }




}
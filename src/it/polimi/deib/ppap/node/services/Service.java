package it.polimi.deib.ppap.node.services;

public class Service implements Comparable{

    private String id;
    private long memory;
    private float targetAllocation;
    private float SLA;

    public Service(String id, long memory, float SLA){
        this.id = id;
        this.memory = memory;
        this.SLA = SLA;
    }

    public Service(Service copy){
        this.id = copy.id;
        this.memory = copy.memory;
        this.SLA = copy.SLA;
    }

    public long getMemory() {
        return memory;
    }

    public String getId() {
        return id;
    }

    public void setTargetAllocation(float targetAllocation) {
        this.targetAllocation = targetAllocation;
    }

    public float getTargetAllocation() {
        return targetAllocation;
    }

    public float getSLA() {
        return SLA;
    }

    @Override
    public boolean equals(Object o) {
        return id.equals(((Service) o).id);
    }
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public int compareTo(Object o) {
        return this.id.compareTo(((Service)o).getId());
    }

    @Override
    public String toString(){
        return "Service "+id;
    }
}
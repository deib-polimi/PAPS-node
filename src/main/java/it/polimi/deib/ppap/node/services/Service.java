package it.polimi.deib.ppap.node.services;

public class Service implements Comparable {

    private String id;
    private long memory;
    private float targetAllocation;
    private float RT;
    private float ET;

    public Service(String id, long memory, float RT, float ET){
        this.id = id;
        this.memory = memory;
        this.RT = RT;
        this.ET = ET;
    }

    public Service(Service copy){
        this.id = copy.id;
        this.memory = copy.memory;
        this.RT = copy.RT;
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

    public float getRT() {
        return RT;
    }

    public float getET() {
        return ET;
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
package it.polimi.deib.ppap.node.resources;

public interface TaskListener<A extends Runnable> {
    void taskEnqueued(A task);
    void taskStarted(A task);
    void taskExecuted(A task);
}

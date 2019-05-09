package it.polimi.deib.ppap.node.control;

import it.polimi.deib.ppap.node.monitoring.MonitoringData;
import it.polimi.deib.ppap.node.services.Service;

import java.util.Random;

public class Planner
{

    private int step=0;
    private final int CORE_MIN = 1;
    private final Random random = new Random();

    private final float A1_NOM = 0.1963f;
    private final float A2_NOM = 0.002f;
    private final float A3_NOM = 0.5658f;
    private final float SLA;
    private final float P_NOM = 0.8f;
    private final float A;

    private float uiOld = 0.0f;
    private final Service service;

    private float req;
    private float ke;
    private float core;

    public Planner(Service service, float alpha)
    {
        A = alpha;
        SLA = service.getSLA();
        this.service = service;
    }

    public float nextResourceAllocation(MonitoringData data)
    {

        step++;

        req = (float) data.getRequests();
        float rt = data.getResponseTime();
        float e = SLA/1000 - rt/1000;
        ke = (A-1)/(P_NOM-1)*e;
        float ui = uiOld+(1-P_NOM)*ke;
        float ut = ui+ke;

        core = req*(ut-A1_NOM-1000.0f*A2_NOM)/(1000.0f*A3_NOM*(A1_NOM-ut));

        float approxCore = Math.max(Math.abs(core), CORE_MIN);

        float approxUt = ((1000.0f*A2_NOM+A1_NOM)*req+1000.0f*A1_NOM*A3_NOM*approxCore)/(req+1000.0f*A3_NOM*approxCore);

        System.out.println("*Control planner - "+service.getId()+", step "+step+"*\nCurrent rt: "+rt+"\nCurrent users: "+req+"\nSLA is set to: "+SLA+"\nError is: "+e+"\nke is: "+ke+"\nUi, UiOld, Utilde and approxUtilde are: "+ui+" "+uiOld+" "+ut+" "+approxUt+"\nCore and approxCore are: "+core+" "+approxCore+"\n");

        uiOld = approxUt-ke;

        return approxCore;

    }

    public void updateState(float allocatedCore) {
        System.out.println("Allocated CTNs: "+allocatedCore);
        float approxUt = ((1000.0f * A2_NOM + A1_NOM) * req +
                1000.0f * A1_NOM * A3_NOM * allocatedCore) / (req + 1000.0f * A3_NOM * allocatedCore); // recompute PI contribution

        uiOld = approxUt - ke; // update integral contribution
    }

    public float lastOptimalAllocation(){
        return core;
    }

}

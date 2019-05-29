package it.polimi.deib.ppap.node.commons;

import it.polimi.deib.ppap.node.NodeFacade;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Utils {

    public static Logger getLogger(String fileName){

        Logger res = null;
        try {
            System.setProperty("java.util.logging.SimpleFormatter.format",
                    "%5$s%6$s%n");
            res = Logger.getLogger(fileName);
            Handler handler = null;
            handler = new FileHandler(fileName);
            handler.setFormatter(new SimpleFormatter());
            res.addHandler(handler);
        } catch (IOException e) {
                e.printStackTrace();
        }

        res.info(NodeFacade.LOG_HEADER);


        return res;
    }


    public static NormalDistribution getNormalDistribution(double mean, double std){
        return new NormalDistribution(mean, std);
    }

}

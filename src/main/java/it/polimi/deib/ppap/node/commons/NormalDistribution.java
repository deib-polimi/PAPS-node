package it.polimi.deib.ppap.node.commons;

import java.util.Random;

public class NormalDistribution {

    private double mean;
    private double std;

    private Random r;

    protected NormalDistribution(double mean, double std){
        this.mean = mean;
        this.std = std;
        r = new Random(System.currentTimeMillis());
    }

    public double random() {
        // https://stackoverflow.com/questions/6011943/java-normal-distribution
        return r.nextGaussian()*std+mean;
    }


    public static void main(String[] args){
        NormalDistribution n = new NormalDistribution(50, 1);
        for (int i = 0; i < 100; i++)
            System.out.println(n.random());
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.Serializable;

/**
 *
 * @author hiroki
 */

public class Perceptron implements Serializable{
    final private int WEIGHT_SIZE = Config.WEIGHT_SIZE;
    public float[] weight;
    public float[] averagedWeight;
    public float step;

    public Perceptron(){
        this.weight = new float[WEIGHT_SIZE];
        this.averagedWeight = new float[WEIGHT_SIZE];
        this.step = 1.0f;
    }
    
    final public float calcScore(int[] featIDs) {
        float score = 0.0f;
        for(int i=0; i<featIDs.length; ++i)
            score += weight[featIDs[i]];
        return score;
    }
    
    final public void updateWeights(int[] oraclePhi, int[] systemPhi) {
        for (int i=0; i<oraclePhi.length; ++i) {
            int phiId = oraclePhi[i];
            this.weight[phiId] += 1.0f;
            this.averagedWeight[phiId] += this.step;
        }
        
        for (int i=0; i<systemPhi.length; ++i) {
            int phiId = systemPhi[i];
            this.weight[phiId] -= 1.0f;
            this.averagedWeight[phiId] -= this.step;
        }
        
        this.step += 1.0f;
    }

    final public float[] getAvgWeight(){
        float[] avgWeight = new float[weight.length];
        for (int i=0; i<weight.length; ++i)
            avgWeight[i] = weight[i] - averagedWeight[i] / step;
        return avgWeight;
    }

}

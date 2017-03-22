/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author hiroki
 */

public class Perceptron implements Serializable{
    public float[] weight;
    public float[] aweight;
    public float t = 1.0f;

    public Perceptron(int nCases, int weightSize){
        this.weight = new float[weightSize];
        this.aweight = new float[weightSize];
    }
    
    public Perceptron() {}

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
            this.aweight[phiId] += this.t;
        }
        
        for (int i=0; i<systemPhi.length; ++i) {
            int phiId = systemPhi[i];
            this.weight[phiId] -= 1.0f;
            this.aweight[phiId] -= this.t;
        }
        
        this.t += 1.0f;
    }
    
}

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
    public Feature feature;
    public ArrayList[][][][] cacheFeats;
    public int sentIndex = 0;
    public float total;
    public float correct;

    public Perceptron(int nCases, int sentIndex, int maxSentLen, int weightSize){
        this.weight = new float[weightSize];
        this.aweight = new float[weightSize];
        this.feature = new Feature(nCases);
        this.feature.weightSize = weight.length;
        this.cacheFeats = new ArrayList[sentIndex][nCases][maxSentLen][maxSentLen];
    }
    
    public Perceptron() {}

    final public float calcScore(ArrayList<Integer> usedFeatures) {
        float score = 0.0f;
        for(int i=0; i<usedFeatures.size(); ++i)
            score += weight[usedFeatures.get(i)];
        return score;
    }
    
    final public void updateWeights(ArrayList<Integer> oraclePhi, ArrayList<Integer> systemPhi) {
        for (int i=0; i<oraclePhi.size(); ++i) {
            int phiId = oraclePhi.get(i);
            this.weight[phiId] += 1.0f;
            this.aweight[phiId] += this.t;
        }
        
        for (int i=0; i<systemPhi.size(); ++i) {
            int phiId = systemPhi.get(i);
            this.weight[phiId] -= 1.0f;
            this.aweight[phiId] -= this.t;
        }
        
        this.t += 1.0f;
    }

    final public void checkAccuracy(int[][] oracleGraph, int[][] systemGraph) {
        for (int i=0; i<oracleGraph.length; ++i) {
            int[] oracleGraph_i = oracleGraph[i];
            int[] systemGraph_i = systemGraph[i];

            for (int j=0; j<oracleGraph_i.length; ++j) {
                if (oracleGraph_i[j] == systemGraph_i[j]) correct += 1.0f;
                total += 1.0f;
            } 
        }
    }
    
}

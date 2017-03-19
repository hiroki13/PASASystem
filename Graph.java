/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author hiroki
 */
public class Graph {
    final public int[][] graph;
    final public int nPrds;
    final public int nCases;

    // 1D: nPrds, 2D: nArgs, 3D: nCases, 4D: nFeats
    final public int[][][][] allFeatIDs;
    final public float[][][] scoreTensor;

    public int[] featIDs;
    public float score;
    
    public Graph(int nPrds, int nArgs, int nCases) {
        this.nPrds = nPrds;
        this.nCases = nCases;
        this.graph = new int[nPrds][nCases];
        this.allFeatIDs = new int[nPrds][nArgs][nCases][];
        this.scoreTensor = new float[nPrds][nArgs][nCases];
    }
    
    final public void addFeatIDs(int[] featIDs, int prdIndex, int argIndex, int caseLabel) {
        allFeatIDs[prdIndex][argIndex][caseLabel] = featIDs;
    }

    final public void addScore(float score, int prdIndex, int argIndex, int caseLabel) {
        scoreTensor[prdIndex][argIndex][caseLabel] = score;
    }
    
    final public void setBestGraph() {
    }
    
}

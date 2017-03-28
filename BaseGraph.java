/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author hiroki
 */
public class BaseGraph extends Graph {
    
    // 1D: nPrds, 2D: nArgs, 3D: nCases, 4D: nFeats
    final public int[][][][] allFeatIDs;
    final public float[][][] scoreTable;
    
    public BaseGraph(int nPrds, int nArgs, int nCases) {
        this.nPrds = nPrds;
        this.nCases = nCases;
        this.bestGraph = new int[nPrds][nCases];
        this.allFeatIDs = new int[nPrds][nArgs][nCases][];
        this.scoreTable = new float[nPrds][nArgs][nCases];
    }
    
    final public void addFeatIDs(int[] featIDs, int prdIndex, int argIndex, int caseLabel) {
        allFeatIDs[prdIndex][argIndex][caseLabel] = featIDs;
    }

    final public void addScore(float score, int prdIndex, int argIndex, int caseLabel) {
        scoreTable[prdIndex][argIndex][caseLabel] = score;
    }
    
    final public void setBestGraph() {
        float[][] bestScores = genInitBestGraphScores();

        for (int prdIndex=0; prdIndex<scoreTable.length; ++prdIndex) {
            float[][] prdScores = scoreTable[prdIndex];
            
            for (int argIndex=0; argIndex<prdScores.length; ++argIndex) {
                float[] argScores = prdScores[argIndex];
                
                for (int caseLabel=0; caseLabel<argScores.length; ++caseLabel) {
                    float score = argScores[caseLabel];
                    float bestScore = bestScores[prdIndex][caseLabel];

                    if (bestScore < score) {
                        bestScores[prdIndex][caseLabel] = score;
                        bestGraph[prdIndex][caseLabel] = argIndex;
                    }
                }
            }
        }
    }
    
    private float[][] genInitBestGraphScores() {
        float[][] graph = new float[nPrds][nCases];
        for (int i=0; i<nPrds; ++i)
            for (int j=0; j<nCases; ++j)
                graph[i][j] = -1000000.0f;
        return graph;
    }
    
    final public void setBestGraphScore() {
        bestGraphScore = 0.0f;
        for (int prdIndex=0; prdIndex<bestGraph.length; ++prdIndex) {
            for (int caseLabel=0; caseLabel<bestGraph[prdIndex].length; ++caseLabel)
                bestGraphScore += scoreTable[prdIndex][bestGraph[prdIndex][caseLabel]][caseLabel];
        }
    }
    
    final public void setBestGraphFeatIDs() {
        int N_FEATS = FeatureExtractor.N_FEATS;
        bestGraphFeatIDs = new int[nPrds * nCases * N_FEATS];
        for (int prdIndex=0; prdIndex<bestGraph.length; ++prdIndex) {
            for (int caseLabel=0; caseLabel<bestGraph[prdIndex].length; ++caseLabel) {
                int[] tmpFeatIDs = allFeatIDs[prdIndex][bestGraph[prdIndex][caseLabel]][caseLabel];
                System.arraycopy(tmpFeatIDs, 0, bestGraphFeatIDs, (prdIndex*nCases+caseLabel)*N_FEATS, N_FEATS);
            }
        }
    }
    
}

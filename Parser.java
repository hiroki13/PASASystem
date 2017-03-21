/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.ArrayList;

/**
 *
 * @author hiroki
 */

public class Parser {

    public Perceptron perceptron;
    public FeatureExtractor featExtractor;
    public boolean hasCache = false;
    public ArrayList[][][] tmpCache;
    public ArrayList<Integer> bestPhi;
    public int nCases;

    public Parser() {}

    public Parser(int nCases) {}
    
    public Parser(int nCases, int rndSeed) {}

    public void train(Sample sample) {
        int[] oracleFeatIDs = getOracleGraphFeatIDs(sample);
        int[] systemFeatIDs = extractBestGraphFeatIDs(sample);        
        perceptron.updateWeights(oracleFeatIDs, systemFeatIDs);
    }
    
    private int[] getOracleGraphFeatIDs(Sample sample) {
        return sample.oracleFeatIDs;
    }

    private int[] extractBestGraphFeatIDs(Sample sample) {
        Graph graph = decode(sample);
        graph.setBestGraph();
        return graph.featIDs;
    }

    public int[][] decode(Sentence sent, int[][] oracleGraph) {
        return new int[][]{};
    }

    public int[][] decode(Sentence sent) {
        return new int[][]{};
    }
    
    public Graph decode(Sample sample) {
        return null;
    }

    public ArrayList getFeature(Sentence sent, int[][] oracleGraph){
        return new ArrayList();
    }

    final public int[] extractUnlabeledFeatIDs(Sample sample, Chunk prd, Chunk arg) {
        return featExtractor.extractUnlabeledFeatIDs(sample, prd, arg);
    }

    final public int[] extractLabeledFeatIDs(Sample sample, Chunk prd, Chunk arg, int caseLabel) {
        return featExtractor.extractLabeledFeatIDs(sample, prd, arg, caseLabel);
    }

    final public float calcScore(int[] featIDs) {
        return perceptron.calcScore(featIDs);
    }
        

}

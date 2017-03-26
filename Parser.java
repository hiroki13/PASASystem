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

    final public int nCases = Config.N_CASES;

    public Perceptron perceptron;
    public FeatureExtractor featExtractor;
    public Evaluator evaluator;

    public boolean hasCache = false;
    public ArrayList[][][] tmpCache;
    public ArrayList<Integer> bestPhi;

    public Parser() {}

    public Parser(int rndSeed) {}

    public void train(Sample sample) {
        Graph graph = decode(sample);
        graph.setBestGraph();

        int[][] oracleGraph = sample.oracleGraph;
        int[][] bestGraph = graph.bestGraph;
        evaluator.update(sample, oracleGraph, bestGraph);

        int[] oracleFeatIDs = getOracleGraphFeatIDs(sample);
        int[] systemFeatIDs = extractBestGraphFeatIDs(graph);
        perceptron.updateWeights(oracleFeatIDs, systemFeatIDs);
    }
    
    public int[][] predict(Sample sample) {
        Graph graph = decode(sample);
        graph.setBestGraph();
        return graph.bestGraph;
    }
    
    private int[] getOracleGraphFeatIDs(Sample sample) {
        return sample.oracleFeatIDs;
    }

    private int[] extractBestGraphFeatIDs(Graph graph) {
        graph.setBestGraphFeatIDs();
        return graph.bestGraphFeatIDs;
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

    final public int[] extractLabeledFeatIDs(Sample sample, Chunk prd, Chunk arg, int prdIndex, int caseLabel) {
        return featExtractor.extractLabeledFeatIDs(sample, prd, arg, prdIndex, caseLabel);
    }

    final public float calcScore(int[] featIDs) {
        return perceptron.calcScore(featIDs);
    }
        

}

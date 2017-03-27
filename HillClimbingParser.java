
import java.util.ArrayList;
import java.util.Random;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author hiroki
 */

public class HillClimbingParser extends Parser {
    
    final private int RESTART;
    final private Random rnd;

    public HillClimbingParser(int restart, int rndSeed) {
        this.rnd = setRndSeed(rndSeed);
        this.RESTART = restart;
        this.perceptron = new Perceptron();
    }

    private Random setRndSeed(int rndSeed) {
        if (rndSeed == 0)
            return new Random();        
        return new Random(rndSeed);
    }

    @Override
    final public Graph decode(Sample sample) {
        int nPrds = sample.prds.length;
        int nArgs = sample.args.length;
        int[][][][][][][] feats = getAllFeats(sample);

        int[][] graph = null;
        int[][] bestGraph = genInitGraph(nPrds, nArgs, N_CASES);

        while(graph != bestGraph) {
            graph = bestGraph;
            ArrayList<int[][]> neighborGraphs = getNeighborGraphs(graph);
            bestGraph = getBestGraph(graph, neighborGraphs);
        }
                
        return new HillGraph(bestGraph, feats);
    }
    
    private int[][] genInitGraph(int nPrds, int nArgs, int nCases) {
        int[][] graph = new int[nPrds][nCases];
        return graph;
    }
    
    private int[][] getBestGraph(int[][] graph, ArrayList<int[][]> neighborGraphs) {
        int[][] bestGraph = null;
        return bestGraph;
    }
    
    private ArrayList<int[][]> getNeighborGraphs(int[][] graph) {
        ArrayList<int[][]> neighborGraphs = new ArrayList();
        return neighborGraphs;
    }
    
    private int[][][][][][][] getAllFeats(Sample sample) {
        int nPrds = sample.prds.length;
        int nArgs = sample.args.length;
        int[][][][][][][] feats = new int[nPrds][nArgs][N_CASES][nPrds][nArgs][N_CASES][];

        for (int prdIndex1=0; prdIndex1<nPrds; ++prdIndex1) {
            Chunk prd1 = sample.prds[prdIndex1];

            for (int argIndex1=0; argIndex1<nArgs; ++argIndex1) {
                Chunk arg1 = sample.args[argIndex1];                

                for (int caseLabel1=0; caseLabel1<N_CASES; ++caseLabel1) {

                    for (int prdIndex2=prdIndex1; prdIndex2<nPrds; ++prdIndex2) {
                        Chunk prd2 = sample.prds[prdIndex2];
                        Chunk[] prds = getReorderPrds(prd1, prd2);

                        for (int argIndex2=argIndex1; argIndex2<nArgs; ++argIndex2) {
                            Chunk arg2 = sample.args[argIndex2];
                            Chunk[] args = getReorderArgs(arg1, arg2);
                        
                            for (int caseLabel2=0; caseLabel2<N_CASES; ++caseLabel2)
                                feats[prdIndex1][prdIndex2][argIndex1][argIndex2][caseLabel1][caseLabel2] =
                                        extractLabeledFeatIDs(sample, prds, args, new int[]{caseLabel1, caseLabel2});
                        }
                    }
                }
            }
        }
        return feats;
    }
    
    final public int[] extractLabeledFeatIDs(Sample sample, Chunk[] prds, Chunk[] args, int[] caseLabels) {
        return featExtractor.extractLabeledFeatIDs(sample, prds, args, caseLabels);
    }

    private Chunk[] getReorderPrds(Chunk prd1, Chunk prd2) {
        if (prd1.prd.FORM.hashCode() < prd2.prd.hashCode())
            return new Chunk[]{prd1, prd2};
        return new Chunk[]{prd2, prd1};
    }

    private Chunk[] getReorderArgs(Chunk arg1, Chunk arg2) {
        if (arg1.chead.FORM.hashCode() < arg2.chead.FORM.hashCode())
            return new Chunk[]{arg1, arg2};
        return new Chunk[]{arg2, arg1};
    }

}

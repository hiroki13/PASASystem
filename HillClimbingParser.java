
import java.util.HashMap;
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
        this.featExtractor = new FeatureExtractor();
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

        int[][] bestGraph = null;
        int[][] graph = genInitGraph(nPrds, nArgs);
        ScoreTable scoreTable = getScoreTable(sample);

        while(true) {
            bestGraph = getBestGraph(graph, scoreTable, nArgs);
            if (Graph.isEqualGraph(graph, bestGraph))
                break;
            graph = bestGraph;
        }
        
        return new HillGraph(bestGraph, getFeatIDs(bestGraph, scoreTable));
    }
    
    private ScoreTable getScoreTable(Sample sample) {
        ScoreTable scoreTable = new ScoreTable(sample.prds.length, sample.args.length);
        scoreTable = setLocalScoreTable(sample, scoreTable);
        scoreTable = setGlobalScoreTable(sample, scoreTable);
        return scoreTable;
    }
    
    private ScoreTable setLocalScoreTable(Sample sample, ScoreTable scoreTable) {
        int nPrds = sample.prds.length;
        int nArgs = sample.args.length;

        for (int prdIndex=0; prdIndex<nPrds; ++prdIndex) {
            Chunk prd = sample.prds[prdIndex];            

            for (int argIndex=0; argIndex<nArgs; ++argIndex) {
                Chunk arg = sample.args[argIndex];                

                for (int caseLabel=0; caseLabel<N_CASES; ++caseLabel) {
                    int [] localFeatIDs = extractLocalFeatIDs(sample, prd, arg, prdIndex, caseLabel);
                    scoreTable.localFeatIDs[prdIndex][argIndex][caseLabel] = localFeatIDs;
                    scoreTable.localScores[prdIndex][argIndex][caseLabel] = getScore(localFeatIDs);
                }
            }
        }
        return scoreTable;
    }

    private ScoreTable setGlobalScoreTable(Sample sample, ScoreTable scoreTable) {
        int nPrds = sample.prds.length;
        int nArgs = sample.args.length;

        for (int prdIndex1=0; prdIndex1<nPrds; ++prdIndex1) {
            Chunk prd1 = sample.prds[prdIndex1];
            int[][][][][][] feats1 = scoreTable.globalFeatIDs[prdIndex1];
            float[][][][][] scores1 = scoreTable.globalScores[prdIndex1];

            for (int argIndex1=0; argIndex1<nArgs; ++argIndex1) {
                Chunk arg1 = sample.args[argIndex1];                
                int[][][][][] feats2 = feats1[argIndex1];
                float[][][][] scores2 = scores1[argIndex1];

                for (int caseLabel1=0; caseLabel1<N_CASES; ++caseLabel1) {
                    int[][][][] feats3 = feats2[caseLabel1];
                    float[][][] scores3 = scores2[caseLabel1];

                    for (int prdIndex2=prdIndex1; prdIndex2<nPrds; ++prdIndex2) {
                        Chunk prd2 = sample.prds[prdIndex2];
                        Chunk[] prds = getReorderPrds(prd1, prd2);
                        int[][][] feats4 = feats3[prdIndex2];
                        float[][] scores4 = scores3[prdIndex2];

                        int initCaseLabel = 0;
                        if (prdIndex1 == prdIndex2)
                            initCaseLabel = caseLabel1+1;

                        for (int argIndex2=0; argIndex2<nArgs; ++argIndex2) {
                            Chunk arg2 = sample.args[argIndex2];
                            Chunk[] args = getReorderArgs(arg1, arg2);
                            int[][] feats5 = feats4[argIndex2];
                            float[] scores5 = scores4[argIndex2];
                        
                            for (int caseLabel2=initCaseLabel; caseLabel2<N_CASES; ++caseLabel2) {
                                int[] globalFeatIDs = extractGlobalFeatIDs(sample, prds, args, new int[]{caseLabel1, caseLabel2});
                                feats5[caseLabel2] = globalFeatIDs;
                                scores5[caseLabel2] = getScore(globalFeatIDs);
                            }
                        }
                    }
                }
            }
        }
        return scoreTable;
    }
    
    private int[] getFeatIDs(int[][] graph, ScoreTable scoreTable) {
        int[] localFeatIDs = getLocalFeatIDs(graph, scoreTable);
        int[] globalFeatIDs = getGlobalFeatIDs(graph, scoreTable);
        return concatArrays(localFeatIDs, globalFeatIDs);
    }

    private int[] getLocalFeatIDs(int[][] graph, ScoreTable scoreTable) {
        return featExtractor.getLocalFeatIDs(graph, scoreTable);
    }

    private int[] getGlobalFeatIDs(int[][] graph, ScoreTable scoreTable) {
        return featExtractor.getGlobalFeatIDs(graph, scoreTable);
    }
    
    private int[][] getBestGraph(int[][] graph, ScoreTable scoreTable, int nArgs) {
        int[][] bestGraph = null;
        float bestScore = Float.NEGATIVE_INFINITY;

        for (int prdIndex=0; prdIndex<graph.length; ++prdIndex) {
            int[] tmpGraph = graph[prdIndex];
            
            for (int caseLabel=0; caseLabel<tmpGraph.length; ++caseLabel) {

                for (int argIndex=0; argIndex<nArgs; ++argIndex) {
                    int[][] neighborGraph = copyGraph(graph);
                    neighborGraph[prdIndex][caseLabel] = argIndex;
                    float localScore = getLocalScore(neighborGraph, scoreTable);
                    float globalScore = getGlobalScore(neighborGraph, scoreTable);
                    float score = localScore + globalScore;

                    if (bestScore < score) {
                        bestScore = score;
                        bestGraph = neighborGraph;
                    }
                }                
            }
        }
        return bestGraph;
    }
    
    private int[][] copyGraph(int[][] graph) {
        int[][] newGraph = new int[graph.length][];
        for (int i=0; i<graph.length; ++i)
            newGraph[i] = graph[i].clone();
        return newGraph;
    }
    
    private float getLocalScore(int[][] graph, ScoreTable scoreTable) {
        float score = 0.0f;
        int nPrds = graph.length;

        for (int prdIndex=0; prdIndex<nPrds; ++prdIndex) {
            int[] tmpGraph = graph[prdIndex];
            float[][] scores = scoreTable.localScores[prdIndex];

            for (int caseLabel=0; caseLabel<N_CASES; ++caseLabel) {
                int argIndex = tmpGraph[caseLabel];
                score += scores[argIndex][caseLabel];
            }
        }        
        return score;
    }
    
    private float getGlobalScore(int[][] graph, ScoreTable scoreTable) {
        float score = 0.0f;
        int nPrds = graph.length;

        for (int prdIndex1=0; prdIndex1<nPrds; ++prdIndex1) {
            int[] tmpGraph1 = graph[prdIndex1];
            float[][][][][] scores1 = scoreTable.globalScores[prdIndex1];

            for (int caseLabel1=0; caseLabel1<N_CASES; ++caseLabel1) {
                int argIndex1 = tmpGraph1[caseLabel1];
                float[][][] scores2 = scores1[argIndex1][caseLabel1];

                for (int prdIndex2=prdIndex1; prdIndex2<nPrds; ++prdIndex2) {
                    int[] tmpGraph2 = graph[prdIndex2];
                    float[][] scores3 = scores2[prdIndex2];
                    
                    int initCaseLabel = 0;
                    if (prdIndex1 == prdIndex2)
                        initCaseLabel = caseLabel1+1;

                    for (int caseLabel2=initCaseLabel; caseLabel2<N_CASES; ++caseLabel2) {
                        int argIndex2 = tmpGraph2[caseLabel2];
                        score += scores3[argIndex2][caseLabel2];
                    }
                }
            }
        }
        return score;
    }
        
    private int[] concatArrays(int[] array1, int[] array2, int[] array3) {
        int[] array = new int[array1.length + array2.length + array3.length];
        System.arraycopy(array1, 0, array, 0, array1.length);
        System.arraycopy(array2, 0, array, array1.length, array2.length);
        System.arraycopy(array3, 0, array, array1.length + array2.length, array3.length);
        return array;
    }
    
    private int[] concatArrays(int[] array1, int[] array2) {
        int[] array = new int[array1.length + array2.length];
        System.arraycopy(array1, 0, array, 0, array1.length);
        System.arraycopy(array2, 0, array, array1.length, array2.length);
        return array;
    }
    
    final public int[] extractGlobalFeatIDs(Sample sample, Chunk[] prds, Chunk[] args, int[] caseLabels) {
        return featExtractor.extractGlobalFeatIDs(sample, prds, args, caseLabels);
    }

    private Chunk[] getReorderPrds(Chunk prd1, Chunk prd2) {
        if (prd1.prd.FORM.hashCode() < prd2.prd.FORM.hashCode())
            return new Chunk[]{prd1, prd2};
        return new Chunk[]{prd2, prd1};
    }

    private Chunk[] getReorderArgs(Chunk arg1, Chunk arg2) {
        if (arg1.chead.FORM.hashCode() < arg2.chead.FORM.hashCode())
            return new Chunk[]{arg1, arg2};
        return new Chunk[]{arg2, arg1};
    }
    
    private float getScore(int[] featIDs) {
        return perceptron.calcScore(featIDs);
    }

    private int[][] genInitGraph(int nPrds, int nArgs) {
        int[][] graph = new int[nPrds][];
        for (int prdIndex=0; prdIndex<nPrds; ++prdIndex)
            graph[prdIndex] = genEachInitGraph(nArgs);
        return graph;
    }
    
    private int[] genEachInitGraph(int nArgs) {
        int[] graph = new int[N_CASES];        
        for (int caseLabel=0; caseLabel<N_CASES; ++caseLabel)
            graph[caseLabel] = rnd.nextInt(nArgs);
        return graph;
    }
    
}

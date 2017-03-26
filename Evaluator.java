/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author hiroki
 */

public class Evaluator {
    final public int N_CASES = Config.N_CASES;
    final public int N_TYPES = 2;
    final public int DEP = Word.DEP;
    final public int INTRA_ZERO = Word.INTRA_ZERO;

    public int crr = 0;
    public int ttl = 0;

    final public int[][] correct;
    final public int[][] oracleTotal;
    final public int[][] systemTotal;
    
    public Evaluator(){
        this.correct = new int[N_CASES+1][N_TYPES+1];
        this.oracleTotal = new int[N_CASES+1][N_TYPES+1];
        this.systemTotal = new int[N_CASES+1][N_TYPES+1];
    }
    
    final public void updateAccuracy(int[][] oracleGraph, int[][] systemGraph) {
        if (oracleGraph.length != systemGraph.length)
            System.err.print("ERROR");
        
        for (int prdIndex=0; prdIndex<oracleGraph.length; ++prdIndex) {
            for (int caseLabel=0; caseLabel<oracleGraph[prdIndex].length; ++caseLabel) {
                int oracleArgIndex = oracleGraph[prdIndex][caseLabel];
                int systemArgIndex = systemGraph[prdIndex][caseLabel];
                
                if (oracleArgIndex == systemArgIndex)
                    crr += 1;
                ttl += 1;
            }
        }
    }
    
    final public void showAccuracy() {
        float acc = crr / (float) ttl;
        System.out.println(String.format("\nACC: %f (%d/%d)", acc, crr, ttl));
    }
    
    final public void update(Sample sample, int[][] oracleGraph, int[][] systemGraph) {
        if (oracleGraph.length != systemGraph.length)
            System.err.print("ERROR");

        int NULL_ARG_INDEX = sample.NULL_ARG_INDEX;
        for (int prdIndex=0; prdIndex<oracleGraph.length; ++prdIndex) {
            Chunk prd = sample.prds[prdIndex];
            
            for (int caseLabel=0; caseLabel<oracleGraph[prdIndex].length; ++caseLabel) {
                Chunk oracleArg = sample.args[oracleGraph[prdIndex][caseLabel]];
                Chunk systemArg = sample.args[systemGraph[prdIndex][caseLabel]];

                updateTotal(oracleTotal[caseLabel], prd, oracleArg, NULL_ARG_INDEX);
                updateTotal(systemTotal[caseLabel], prd, systemArg, NULL_ARG_INDEX);
                updateCorrect(correct[caseLabel], prd, oracleArg, systemArg, NULL_ARG_INDEX);
            }
        }
    }
    
    final public void show() {
        float[][][] metrics = summarize();
        for (int caseLabel=0; caseLabel<metrics.length; ++caseLabel) {
            System.out.println(String.format("\n\tCase:%s", getCaseName(caseLabel)));

            for (int argType=0; argType<metrics[caseLabel].length; ++argType) {
                float[] scores = metrics[caseLabel][argType];
                float p = scores[0];
                float r = scores[1];
                float f = scores[2];
                int crr = correct[caseLabel][argType];
                int orcttl = oracleTotal[caseLabel][argType];
                int systtl = systemTotal[caseLabel][argType];

                String text = String.format("\t\t%s:  F:%f  P:%f (%d/%d)  R:%f (%d/%d)",
                        getArgTypeName(argType), f, p, crr, systtl, r, crr, orcttl);
                System.out.println(text);
            }
        }
    }
    
    private String getCaseName(int caseLabel) {
        if (caseLabel == 0)
            return "GA";
        else if (caseLabel == 1)
            return "WO";
        else if (caseLabel == 2)
            return "NI";
        return "ALL";
    }
    
    private String getArgTypeName(int argType) {
        if (argType == 0)
            return "DEP ";
        else if (argType == 1)
            return "ZERO";
        return "ALL ";
    }
    
    private float[][][] summarize() {
        float[][][] metrics = new float[N_CASES+1][N_TYPES+1][];

        for (int caseLabel=0; caseLabel<N_CASES; ++caseLabel) {
            metrics[caseLabel][N_TYPES] = summarizeEachCase(correct[caseLabel],
                                                            oracleTotal[caseLabel],
                                                            systemTotal[caseLabel]);

            for (int argType=0; argType<N_TYPES; ++argType)
                metrics[caseLabel][argType] = calcMetrics(correct[caseLabel][argType],
                                                          oracleTotal[caseLabel][argType],
                                                          systemTotal[caseLabel][argType]);
        }
        metrics[N_CASES] = summarizeAllCases();
        return metrics;
    }
    
    private float[] summarizeEachCase(int[] correct, int[] oracleTotal, int[] systemTotal) {
        for (int argType=0; argType<N_TYPES; ++argType) {
            correct[N_TYPES] += correct[argType];
            oracleTotal[N_TYPES] += oracleTotal[argType];
            systemTotal[N_TYPES] += systemTotal[argType];
        }
        return calcMetrics(correct[N_TYPES], oracleTotal[N_TYPES], systemTotal[N_TYPES]);
    }
    
    private float[][] summarizeAllCases() {
        for (int caseLabel=0; caseLabel<N_CASES; ++caseLabel) {
            for (int argType=0; argType<N_TYPES; ++argType) {
                correct[N_CASES][argType] += correct[caseLabel][argType];
                oracleTotal[N_CASES][argType] += oracleTotal[caseLabel][argType];
                systemTotal[N_CASES][argType] += systemTotal[caseLabel][argType];
            }
        }

        float[][] metrics = new float[N_TYPES+1][];
        for (int argType=0; argType<N_TYPES; ++argType) {
            correct[N_CASES][N_TYPES] += correct[N_CASES][argType];
            oracleTotal[N_CASES][N_TYPES] += oracleTotal[N_CASES][argType];
            systemTotal[N_CASES][N_TYPES] += systemTotal[N_CASES][argType];
            metrics[argType] = calcMetrics(correct[N_CASES][argType],
                                           oracleTotal[N_CASES][argType],
                                           systemTotal[N_CASES][argType]);
        }
        metrics[N_TYPES] = calcMetrics(correct[N_CASES][N_TYPES],
                                       oracleTotal[N_CASES][N_TYPES],
                                       systemTotal[N_CASES][N_TYPES]);
        return metrics;
    }
    
    private float[] calcMetrics(int correct, int oracleTotal, int systemTotal) {
        float recall = correct / (float) oracleTotal;
        float precision = correct / (float) systemTotal;
        float f1 = (2 * recall * precision) / (recall + precision);
        return new float[]{precision, recall, f1};
    }

    private void updateTotal(int[] total, Chunk prd, Chunk arg, int nullArgIndex) {
        if (arg.INDEX != nullArgIndex)
            if (hasDep(prd, arg))
                total[DEP] += 1;
            else
                total[INTRA_ZERO] += 1;        
    }
    
    private void updateCorrect(int[] correct, Chunk prd, Chunk oracleArg, Chunk systemArg, int nullArgIndex) {
        if (oracleArg.INDEX != nullArgIndex && oracleArg.INDEX == systemArg.INDEX)
            if (hasDep(prd, oracleArg))
                correct[DEP] += 1;
            else
                correct[INTRA_ZERO] += 1;
    }

    private boolean hasDep(Chunk prd, Chunk arg) {
        return prd.INDEX == arg.DEP_HEAD || prd.DEP_HEAD == arg.INDEX;
    }
    
    private float[] getAvgWeight(Parser parser){
        Perceptron p = parser.perceptron;
        float[] avgWeight = new float[p.weight.length];
        for (int i=0; i<p.weight.length; ++i)
            avgWeight[i] = p.weight[i] - p.aweight[i] /p.t;
        return avgWeight;
    }
    
}

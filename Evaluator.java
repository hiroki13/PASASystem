/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author hiroki
 */

public class Evaluator {
    public int crr = 0;
    public int ttl = 0;

    public float[] correct;
    public float[] correct_zero;

    public float[] r_dep;
    public float[] r_zero;
    public float[] p_total;
    public float[] p_zero;

    public Parser parser;
    public float alpha;
    public int nCases;
    
    public long time;
    
    public Evaluator(int nCases){
        this.nCases = nCases;
        this.correct = new float[nCases];
        this.correct_zero = new float[nCases];
        this.r_dep = new float[nCases];
        this.r_zero = new float[nCases];
        this.p_total = new float[nCases];
        this.p_zero = new float[nCases];
    }
    
    final public void updataAccuracy(int[][] oracleGraph, int[][] systemGraph) {
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
        System.out.println(String.format("\nACC: %f (%d/%d)\n", acc, crr, ttl));
    }

    final public void setEval(Sentence sent, int[][] graph) {
        ArrayList<Chunk> chunks = sent.chunks;
        ArrayList<Integer> argIndices = sent.argIndices;
        ArrayList<Integer> prdIndices = sent.prdIndices;
        int[][] oracleGraph = sent.oracleGraph;
        int[] oracleCaseGraph = new int[graph.length];
        int[] systemCaseGraph = new int[graph.length];
        
        for (int caseLabel=0; caseLabel<nCases; caseLabel++) {

            for (int prd_i=0; prd_i<graph.length; prd_i++) {
                oracleCaseGraph[prd_i] = oracleGraph[prd_i][caseLabel];
                systemCaseGraph[prd_i] = graph[prd_i][caseLabel];
            }
            
            r_dep[caseLabel] += sent.nDepCaseArgs[caseLabel];
            r_zero[caseLabel] += sent.nZeroCaseArgs[caseLabel];

            eval(chunks, argIndices, prdIndices, oracleCaseGraph, systemCaseGraph, caseLabel);
        }
    }
    
    final public void setWordEval(Sentence sent, int[][] graph) {
        ArrayList<Word> words = sent.words;
        ArrayList<Integer> argIndices = sent.argIndices;
        ArrayList<Integer> prdIndices = sent.prdIndices;
        int[][] oracleGraph = sent.oracleGraph;
        int[] oracleCaseGraph = new int[graph.length];
        int[] systemCaseGraph = new int[graph.length];
        
        for (int caseLabel=0; caseLabel<nCases; caseLabel++) {

            for (int prd_i=0; prd_i<graph.length; prd_i++) {
                oracleCaseGraph[prd_i] = oracleGraph[prd_i][caseLabel];
                systemCaseGraph[prd_i] = graph[prd_i][caseLabel];
            }
            
            r_dep[caseLabel] += sent.nDepCaseArgs[caseLabel];
            r_zero[caseLabel] += sent.nZeroCaseArgs[caseLabel];

            wordEval(words, argIndices, prdIndices, oracleCaseGraph, systemCaseGraph, caseLabel);
        }
    }
    
    private void eval(ArrayList<Chunk> chunks, ArrayList<Integer> argIndices,
                      ArrayList<Integer> prdIndices, int[] oracleGraph,
                      int[] systemGraph, int caseLabel) {
        int zeroArg;
        
        for (int prd_i=0; prd_i<systemGraph.length; ++prd_i) {
            int systemArgIndex = systemGraph[prd_i];
            int oracleArgIndex = oracleGraph[prd_i];
            
            Chunk prd = chunks.get(prdIndices.get(prd_i));
            Chunk arg = chunks.get(systemArgIndex);
            
            if (caseLabel == 0)
                zeroArg = prd.zeroGa;
            else if (caseLabel == 1)
                zeroArg = prd.zeroO;
            else
                zeroArg = prd.zeroNi;
            
            if (systemArgIndex != argIndices.size()-1) {
                p_total[caseLabel] += 1.0f;

                if (arg.HEAD != prd.INDEX && prd.HEAD != arg.INDEX)
                    p_zero[caseLabel] += 1.0f;

                if (oracleArgIndex == systemArgIndex) {
                    correct[caseLabel] += 1.0f;
                    
                    if (systemArgIndex == zeroArg)
                        correct_zero[caseLabel] += 1.0f;
                }
                
            }
        }
    }
    
    private void wordEval(ArrayList<Word> words, ArrayList<Integer> argIndices,
                           ArrayList<Integer> prdIndices, int[] oracleGraph,
                           int[] systemGraph, int caseLabel) {
        int zeroArg;
        
        for (int prd_i=0; prd_i<systemGraph.length; ++prd_i) {
            int systemArgIndex = systemGraph[prd_i];
            int oracleArgIndex = oracleGraph[prd_i];
            
            Word prd = words.get(prdIndices.get(prd_i));
            Word arg = words.get(systemArgIndex);
            Chunk chunk_p = prd.CHUNK;
            Chunk chunk_a = arg.CHUNK;
            
            if (caseLabel == 0)
                zeroArg = prd.zeroGa;
            else if (caseLabel == 1)
                zeroArg = prd.zeroO;
            else
                zeroArg = prd.zeroNi;
            
            if (systemArgIndex != argIndices.size()-1) {
                p_total[caseLabel] += 1.0f;

                if (chunk_a.HEAD != chunk_p.INDEX && chunk_p.HEAD != chunk_a.INDEX)
                    p_zero[caseLabel] += 1.0f;

                if (oracleArgIndex == systemArgIndex) {
                    correct[caseLabel] += 1.0f;
                    if (systemArgIndex == zeroArg)
                        correct_zero[caseLabel] += 1.0f;
                }
                
            }
        }
    }
    
    final public void test(ArrayList<Sentence> sents, Parser parser, int restart, boolean isAvgWeight){
        this.parser = new HillClimbingParser(nCases);
        this.parser.perceptron.feature = parser.perceptron.feature;
        this.parser.perceptron.feature.sents = sents;
        this.parser.perceptron.feature.weightSize = parser.perceptron.weight.length;
        this.parser.hasCache = false;
        this.parser.rnd = parser.rnd;        

        if (isAvgWeight)
            this.parser.perceptron.weight = getAvgWeight(parser);            
        else
            this.parser.perceptron.weight = parser.perceptron.weight;
        
        time = (long) 0.0;

        for (int i=0; i<sents.size(); i++){
            Sentence sent = sents.get(i);
            int nArgs = sent.argIndices.size();
            int nPrds = sent.prdIndices.size();
            this.parser.perceptron.feature.cache = new ArrayList[nCases][nCases][nPrds][nArgs][nPrds][nArgs];

            if (!sent.hasPrds) {
                if (i%1000 == 0 && i != 0)
                    System.out.print(String.format("%d ", i));
                continue;
            }

            long time1 = System.currentTimeMillis();
            int[][] graph = this.parser.decode(sent, restart, true);
            time += System.currentTimeMillis() - time1;
            
            this.setEval(sent, graph);

            if (i%1000 == 0 && i != 0)
                System.out.print(String.format("%d ", i));
        }
    }    
    

    final public void test(ArrayList<Sentence> sents, Parser parser, boolean isAvgWeight){
        this.parser = new BaselineParser(nCases);
        this.parser.perceptron.feature = parser.perceptron.feature;
        this.parser.perceptron.feature.sents = sents;
        this.parser.perceptron.feature.weightSize = parser.perceptron.weight.length;
        this.parser.hasCache = false;

        if (isAvgWeight)
            this.parser.perceptron.weight = getAvgWeight(parser);            
        else
            this.parser.perceptron.weight = parser.perceptron.weight;
        
        time = (long) 0.0;

        for (int i=0; i<sents.size(); i++){
            Sentence sent = sents.get(i);

            if (!sent.hasPrds) {
                if (i%1000 == 0 && i != 0)
                    System.out.print(String.format("%d ", i));
                continue;
            }

            long time1 = System.currentTimeMillis();
            int[][] graph = this.parser.decode(sent);
            time += System.currentTimeMillis() - time1;
            
            setWordEval(sent, graph);

            if (i%1000 == 0 && i != 0)
                System.out.print(String.format("%d ", i));
        }
    }
    

    final public void testAndOutput(ArrayList<Sentence> sents, Parser parser, int restart, String fn,
                                      boolean isModelFile, boolean isAvgWeight) throws IOException{
        this.parser = new Parser(nCases);
        this.parser.perceptron.feature = parser.perceptron.feature;
        this.parser.perceptron.feature.sents = sents;
        this.parser.hasCache = false;
        this.parser.rnd = parser.rnd;

        if (isAvgWeight) {
            this.parser.perceptron.weight = getAvgWeight(parser);            
        }
        else {
            this.parser.perceptron.weight = parser.perceptron.weight;
        }
        
        time = (long) 0.0;

        Sentence sentence;
        int[][][] graph = new int[sents.size()][][];
        int args_length;
        int prds_length;
        
        for (int i=0; i<sents.size(); i++){
            sentence = sents.get(i);
            args_length = sentence.argIndices.size();
            prds_length = sentence.prdIndices.size();
            graph[i] = new int[prds_length][nCases];
            this.parser.perceptron.feature.cache =
                new ArrayList[nCases][nCases][prds_length]
                             [args_length][prds_length][args_length];

            if (!sentence.hasPrds) {
                if (i%1000 == 0 && i != 0)
                    System.out.print(String.format("%d ", i));
                continue;
            }

            long time1 = System.currentTimeMillis();
            graph[i] = this.parser.decode(sentence, restart, true);
            time += System.currentTimeMillis() - time1;

            this.setEval(sentence, graph[i]);

            if (i%1000 == 0 && i != 0)
                System.out.print(String.format("%d ", i));
        }
        
        this.parser.perceptron.feature.cache = null;
        this.parser.perceptron.cacheFeats = null;
        outputText(fn, sents, graph);
        if (isModelFile)
            outputPerceptron(fn);
    }    

    
    final public void outputPerceptron(String fn){
        try {      
            try (ObjectOutputStream objOutStream =
                    new ObjectOutputStream(
                        new FileOutputStream(fn+"_perceptron.bin"))) {
                        objOutStream.writeObject(this.parser.perceptron);
                        objOutStream.close();
            }
      
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    
    final public void outputText(String fn, List<Sentence> sentencelist,
                                  int[][][] graph) throws IOException {
        
        PrintWriter pw = new PrintWriter(new BufferedWriter
                                        (new FileWriter(fn+".ntc")));
        
        int[] null_case =new int[nCases];
        for (int i=0; i<nCases; i++)
            null_case[i] = -1;
        
        for (int i=0; i<sentencelist.size(); ++i) {
            Sentence sentence = sentencelist.get(i);
            ArrayList<Chunk> chunks = sentence.chunks;
            
            pw.println("#");

            int prd_i = 0;
            for (int j=0; j<sentence.size()-1; ++j) {
                Chunk c = chunks.get(j);

                int[] predicted_case;                
                if (c.hasPrd) {
                    predicted_case = graph[i][prd_i];
                    prd_i++;
                }
                else {                     
                    predicted_case = null_case;
                }
                
                for (int k=0; k<predicted_case.length; ++k) {
                    if (predicted_case[k] == sentence.size() - 1)
                        predicted_case[k] = -1;
                }
                                
                String text = String.format("* %d %d | Gold: %s %s %s %s %s %s | System: %s",
                                            c.INDEX, c.HEAD,
                                            Integer.toString(c.ga),
                                            Integer.toString(c.o),
                                            Integer.toString(c.ni),
                                            Integer.toString(c.zeroGa),
                                            Integer.toString(c.zeroO),
                                            Integer.toString(c.zeroNi),
                                            Arrays.toString(predicted_case));
                pw.println(text);           
                
                for (int k=0; k<c.words.size(); ++k) {
                    Word t = (Word) c.words.get(k);
                    text = String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s",
                                        t.FORM, t.PRON, t.REG, t.CPOS,
                                        t.POS, t.INF_TYPE, t.INFL, t.PAS);
                    pw.println(text);                    
                }
            }
            
            pw.println("EOS");
            
        }
        pw.close();
    }

    private float[] getAvgWeight(Parser parser){
        Perceptron p = parser.perceptron;
        float[] avgWeight = new float[p.weight.length];
        for (int i=0; i<p.weight.length; ++i)
            avgWeight[i] = p.weight[i] - p.aweight[i] /p.t;
        return avgWeight;
    }
    
}

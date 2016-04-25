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

public class AccuracyChecker {
    public float[] correct;
    public float[] correct_zero;

    public float[] r_dep;
    public float[] r_zero;
    public float[] p_total;
    public float[] p_zero;

    public Parser parser;
    public float alpha;
    public int case_length;
    
    public long time;
    
    public AccuracyChecker(int c_length){
        case_length = c_length;
        this.correct = new float[case_length];
        this.correct_zero = new float[case_length];
        this.r_dep = new float[case_length];
        this.r_zero = new float[case_length];
        this.p_total = new float[case_length];
        this.p_zero = new float[case_length];
    }

    final public void checkUAS(Sentence sentence, int[][] graph){
        ArrayList<Chunk> chunks = sentence.chunks;
        ArrayList<Integer> arg_ids = sentence.arg_indices;
        int[][] o_graph = sentence.o_graph;
        ArrayList<Integer> prds = sentence.prd_indices;
        int[] o_case_graph = new int[graph.length];
        int[] case_graph = new int[graph.length];
        
        for (int case_label=0; case_label<case_length; case_label++) {

            for (int prd_i=0; prd_i<graph.length; prd_i++) {
                o_case_graph[prd_i] = o_graph[prd_i][case_label];
                case_graph[prd_i] = graph[prd_i][case_label];
            }
            
            r_dep[case_label] += sentence.n_dep_case_args[case_label];
            r_zero[case_label] += sentence.n_zero_case_args[case_label];

            computePerformance(chunks, arg_ids, o_case_graph, case_graph, prds,
                               case_label);
        }
    }
    
    final private void computePerformance(ArrayList<Chunk> chunks,
                                            ArrayList<Integer> arg_ids,
                                            int[] o_graph,
                                            int[] graph,
                                            ArrayList<Integer> prds,
                                            int case_label) {
        int[] zero_arg;
        
        for (int prd_i=0; prd_i<graph.length; ++prd_i) {
            int arg_id = graph[prd_i];
            int o_arg_id = o_graph[prd_i];
            
            Chunk prd = chunks.get(prds.get(prd_i));
            Chunk arg = chunks.get(arg_id);
            
            if (case_label == 0) {
                zero_arg = prd.zero_ga;
            }
            else if (case_label == 1) {
                zero_arg = prd.zero_o;
            }
            else {
                zero_arg = prd.zero_ni;
            }

            
            if (arg_id != arg_ids.size()-1) {
                p_total[case_label] += 1.0f;

                if (arg.head != prd.index && prd.head != arg.index) {
                    p_zero[case_label] += 1.0f;
                }

                if (o_arg_id == arg_id) {
                    correct[case_label] += 1.0f;
                    
                    for (int j=0; j<zero_arg.length; ++j) {
                        if (arg_id == zero_arg[j]) {
                           correct_zero[case_label] += 1.0f;
                           break;
                        }
                    }
                }
                
            }
        }
    }
    
    
    final public void test(List<Sentence> sentencelist,
                            Parser parser,
                            int restart,
                            boolean avg_weight){
        this.parser = new Parser(case_length);
        this.parser.perceptron.feature = parser.perceptron.feature;
        this.parser.perceptron.feature.sentencelist = sentencelist;
        this.parser.perceptron.feature.w = parser.perceptron.weight.length;
        this.parser.cache = false;
        this.parser.rnd = parser.rnd;        

        if (avg_weight) {
            this.parser.perceptron.weight = averagingWeights(parser);            
        }
        else {
            this.parser.perceptron.weight = parser.perceptron.weight;
        }
        
        time = (long) 0.0;

        Sentence sentence;
        int[][] graph;
        int args_length;
        int prds_length;
        
        for (int i=0; i<sentencelist.size(); i++){
            sentence = sentencelist.get(i);
            args_length = sentence.arg_indices.size();
            prds_length = sentence.prd_indices.size();
            this.parser.perceptron.feature.cache =
                new ArrayList[case_length][case_length][prds_length]
                             [args_length][prds_length][args_length];

            if (!sentence.has_prds) {
                if (i%1000 == 0 && i != 0)
                    System.out.print(String.format("%d ", i));
                continue;
            }

            long time1 = System.currentTimeMillis();
            graph = this.parser.decode(sentence, restart, true);
            time += System.currentTimeMillis() - time1;
            
            this.checkUAS(sentence, graph);

            if (i%1000 == 0 && i != 0)
                System.out.print(String.format("%d ", i));
        }
    }    
    

    final public void testAndOutput(List<Sentence> sentencelist,
                                      Parser parser,
                                      int restart,
                                      String fn,
                                      boolean output_model_file,
                                      boolean avg_weight)
                                      throws IOException{
        this.parser = new Parser(case_length);
        this.parser.perceptron.feature = parser.perceptron.feature;
        this.parser.perceptron.feature.sentencelist = sentencelist;
        this.parser.cache = false;
        this.parser.rnd = parser.rnd;

        if (avg_weight) {
            this.parser.perceptron.weight = averagingWeights(parser);            
        }
        else {
            this.parser.perceptron.weight = parser.perceptron.weight;
        }
        
        time = (long) 0.0;

        Sentence sentence;
        int[][][] graph = new int[sentencelist.size()][][];
        int args_length;
        int prds_length;
        
        for (int i=0; i<sentencelist.size(); i++){
            sentence = sentencelist.get(i);
            args_length = sentence.arg_indices.size();
            prds_length = sentence.prd_indices.size();
            graph[i] = new int[prds_length][case_length];
            this.parser.perceptron.feature.cache =
                new ArrayList[case_length][case_length][prds_length]
                             [args_length][prds_length][args_length];

            if (!sentence.has_prds) {
                if (i%1000 == 0 && i != 0)
                    System.out.print(String.format("%d ", i));
                continue;
            }

            long time1 = System.currentTimeMillis();
            graph[i] = this.parser.decode(sentence, restart, true);
            time += System.currentTimeMillis() - time1;

            this.checkUAS(sentence, graph[i]);

            if (i%1000 == 0 && i != 0)
                System.out.print(String.format("%d ", i));
        }
        
        this.parser.perceptron.feature.cache = null;
        this.parser.perceptron.cache_feats = null;
        outputText(fn, sentencelist, graph);
        if (output_model_file)
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
        PrintWriter gw = new PrintWriter(new BufferedWriter
                                        (new FileWriter(fn+".graph")));
        
        int[] null_case =new int[case_length];
        for (int i=0; i<case_length; i++)
            null_case[i] = -1;
        
        for (int i=0; i<sentencelist.size(); i++){
            Sentence sentence = sentencelist.get(i);
            ArrayList<Chunk> chunks = sentence.chunks;
            
            if (sentence.has_prds) {
                for (int j=0; j<graph[i].length; ++j) {
                    String text ="";
                    for (int k=0; k<graph[i][j].length; ++k) {
                        text += graph[i][j][k] + " ";
                    }
                    gw.println(text);
                }
            }
            else {
                gw.println(-1);
            }
            gw.println();
            
            pw.println("#");

            int prd_i = 0;
            for (int j=0; j<sentence.size(); ++j) {
                Chunk c = chunks.get(j);
                int[] predicted_case = new int[case_length];
                
                if (c.pred) {
                    predicted_case = graph[i][prd_i];
                    prd_i++;
                }
                else {                     
                    predicted_case = null_case;
                }
                                
                String text = String.format("* %d %d %s %s %s %s %s %s | pred:%s",
                                            c.index, c.head,
                                            Arrays.toString(c.ga),
                                            Arrays.toString(c.o),
                                            Arrays.toString(c.ni),
                                            Arrays.toString(c.zero_ga),
                                            Arrays.toString(c.zero_o),
                                            Arrays.toString(c.zero_ni),
                                            Arrays.toString(predicted_case));
                pw.println(text);           
                
                for (int k=0; k<c.tokens.size(); ++k) {
                    Token t = (Token) c.tokens.get(k);
                    text = String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s",
                                        t.form, t.yomi, t.r_form, t.cpos,
                                        t.pos, t.inf_type, t.inf_form, t.pas);
                    pw.println(text);                    
                }
            }
            
            pw.println("EOS");
            
        }
        pw.close();
        gw.close();
    }

    
    final private float[] averagingWeights(Parser parser){
        Perceptron p = parser.perceptron;
        float[] new_weight = new float[p.weight.length];
        for (int i=0; i<p.weight.length; ++i) {
            new_weight[i] = p.weight[i] - p.aweight[i] /p.t;
        }
        return new_weight;
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author hiroki
 */

public class Parser {
    public Perceptron perceptron;
    public boolean cache = false;
    public ArrayList[][][] tmp_cache;
    public Random rnd;
    public int case_length;

    public Parser(int c_length, int sent_num, int max_sent_len,
                   int weight_len) {
        this.case_length = c_length;
        this.perceptron = new Perceptron(case_length, sent_num,
                                         max_sent_len, weight_len);
    }

    public Parser(int c_length) {
        this.case_length = c_length;
        this.perceptron = new Perceptron();
    }
    
    final public int[][] decode(final Sentence sentence, final int restart) {
        final int sent_i = sentence.index;
        final int[][] o_graph = sentence.o_graph;
        final int args_length = sentence.arg_indices.size();
        final int prds_length = sentence.prd_indices.size();
        final ArrayList[][][] cache = this.perceptron.cache_feats[sent_i];

        float prev_best_score = -10000000.0f, best_score = -10000000.0f;
        int[][] best_graph = new int[prds_length][case_length];
        int best = -1, best_case_label = -1, best_prd = -1;

        for (int i=0; i<restart; ++i) {
            final int[][] graph = setInitGraph(sentence);

            while (true) {
                final int[][] prev_graph = copyGraph(graph);
                
                for (int prd_i=0; prd_i<prds_length; ++prd_i) {
                    for (int case_label=0; case_label<case_length; case_label++) {
                        for (int arg_i=0; arg_i<args_length; ++arg_i) {                    
                            final int[][] tmp_graph = copyGraph(graph);

                            tmp_graph[prd_i][case_label] = arg_i;
                            float score = getScore(sentence, tmp_graph, cache)
                                          + getHammingDistance(o_graph, tmp_graph);

                            if (score > best_score) {
                                best_score = score;
                                best = arg_i;
                                best_case_label = case_label;
                                best_prd = prd_i;
                            }
                        }                      
                    }
                }
                
                graph[best_prd][best_case_label] = best;
                if (isGraphMatch(graph, prev_graph)) break;
            }
            
            if (best_score > prev_best_score) {
                best_graph = copyGraph(graph);
                prev_best_score = best_score;                
            }            
        }
        
        return best_graph;
    }
    
    final public int[][] decode(final Sentence sentence, final int restart,
                                 final boolean test) {
        this.tmp_cache = new ArrayList[case_length][52][52];
        
        final int args_length = sentence.arg_indices.size();
        final int prds_length = sentence.prd_indices.size();
        final ArrayList[][][] cache = this.tmp_cache;

        float prev_best_score = -100000.0f;
        float best_score = -100000.0f;
        int[][] best_graph = new int[prds_length][case_length];
        int best = -1;
        int best_case_label = -1;        
        int best_prd = -1;

        for (int i=0; i<restart; ++i) {
            final int[][] graph = setInitGraph(sentence);

            while (true) {
                final int[][] prev_graph = copyGraph(graph);
                
                for (int prd_i=0; prd_i<prds_length; ++prd_i) {
                    for (int case_label=0; case_label<case_length; case_label++) {
                        for (int arg_i=0; arg_i<args_length; ++arg_i) {                    
                            final int[][] tmp_graph = copyGraph(graph);
                            
                            tmp_graph[prd_i][case_label] = arg_i;
                            float score = getScore(sentence, tmp_graph, cache);

                            if (score > best_score) {
                                best_score = score;
                                best = arg_i;
                                best_case_label = case_label;
                                best_prd = prd_i;
                            }
                        }                      
                    }
                }
                graph[best_prd][best_case_label] = best;
                if (isGraphMatch(graph, prev_graph)) break;
            }
            
            if (best_score > prev_best_score) {
                best_graph = copyGraph(graph);
                prev_best_score = best_score;                
            }
        }
        
        return best_graph;
    }

    
    final public int[][] decode(final Sentence sentence,
                                 final ArrayList init_graph) {
        final int sent_i = sentence.index;
        final int args_length = sentence.arg_indices.size();
        final int prds_length = sentence.prd_indices.size();
        final ArrayList[][][] cache = this.perceptron.cache_feats[sent_i];

        float best_score = -100000.0f;
        int best = -1, best_case_label = -1, best_prd = -1;

        final int[][] graph = setInitGraph(init_graph);

        while (true) {              
            final int[][] prev_graph = copyGraph(graph);
                
            for (int prd_i=0; prd_i<prds_length; ++prd_i) {     
                for (int case_label=0; case_label<case_length; case_label++) {                
                    for (int arg_i=0; arg_i<args_length; ++arg_i) {                                        
                        final int[][] tmp_graph = copyGraph(graph);                        
                        tmp_graph[prd_i][case_label] = arg_i;                        
                        float score = getScore(sentence, tmp_graph, cache);
                        
                        if (score > best_score) {                        
                            best_score = score;                            
                            best = arg_i;                            
                            best_case_label = case_label;                            
                            best_prd = prd_i;
                        }                        
                    }                                          
                }                
            }
            
            graph[best_prd][best_case_label] = best;            
            if (isGraphMatch(graph, prev_graph)) break;            
        }
        
        return graph;
    }
    
    final public int[][] decode(final Sentence sentence, final boolean test,
                                 final ArrayList init_graph) {
        this.tmp_cache = new ArrayList[case_length][52][52];
        
        final int args_length = sentence.arg_indices.size();
        final int prds_length = sentence.prd_indices.size();
        final ArrayList[][][] cache = this.tmp_cache;

        float best_score = -100000.0f;
        int best = -1, best_case_label = -1, best_prd = -1;
            
        int[][] graph = setInitGraph(sentence);

        while (true) {        
            int[][] prev_graph = copyGraph(graph);
                            
            for (int prd_i=0; prd_i<prds_length; ++prd_i) {            
                for (int case_label=0; case_label<case_length; case_label++) {                
                    for (int arg_i=0; arg_i<args_length; ++arg_i) {                                        
                        int[][] tmp_graph = copyGraph(graph);
                                                    
                        tmp_graph[prd_i][case_label] = arg_i;                        
                        float score = getScore(sentence, tmp_graph, cache);
                        
                        if (score > best_score) {                        
                            best_score = score;                            
                            best = arg_i;                            
                            best_case_label = case_label;                            
                            best_prd = prd_i;                            
                        }
                    }                                          
                }                
            }            
            graph[best_prd][best_case_label] = best;            
            if (isGraphMatch(graph, prev_graph)) break;            
        }
        
        return graph;
    }

    
    
    final private int[][] setInitGraph(final Sentence sentence) {
        final int args_length = sentence.arg_indices.size();
        final int prds_length = sentence.prd_indices.size();
        final int[][] graph = new int[prds_length][case_length];
        
        for (int prd_i=0; prd_i<prds_length; ++prd_i) {
            graph[prd_i] = genGraph(args_length);
        }
        
        return graph;
    }

    final private int[][] setInitGraph(final ArrayList<int[]> graph) {
        final int prds_length = graph.size();
        final int[][] init_graph = new int[prds_length][case_length];
        
        for (int prd_i=0; prd_i<prds_length; ++prd_i) {
            init_graph[prd_i] = graph.get(prd_i);
        }
        
        return init_graph;
    }
    
    final private int[] genGraph(final int args_length) {
        final int[] graph = new int[case_length];
        
        for (int case_label=0; case_label<case_length; ++case_label)
            graph[case_label] = rnd.nextInt(args_length);

        return graph;
    }
    
    final private int[] copyGraph(final int[] graph) {
        final int[] copied_graph = new int[graph.length];
        
        for (int i=0; i<graph.length; ++i)
            copied_graph[i] = graph[i];

        return copied_graph;
    }

    final private int[][] copyGraph(final int[][] graph) {
        final int prds_length = graph[0].length;
        final int[][] copied_graph = new int[graph.length][prds_length];

        for (int i=0; i<graph.length; ++i)
            for (int arg_i=0; arg_i<prds_length; arg_i++)
                copied_graph[i][arg_i] = graph[i][arg_i];

        return copied_graph;
    }
    
    final private boolean isGraphMatch(final int[] graph1,
                                        final int[] graph2) {
        for (int i=0; i<graph1.length; ++i) {           
            if (graph1[i] != graph2[i])            
                return false;
        }
        return true;
    }
    
    final private boolean isGraphMatch(final int[][] graph1,
                                        final int[][] graph2) {
        for (int i=0; i<graph1.length; ++i) {
            final int[] tmp_graph1 = graph1[i];
            final int[] tmp_graph2 = graph2[i];
            
            for (int j=0; j<graph1[0].length; ++j)
                if (tmp_graph1[j] != tmp_graph2[j])
                    return false;
        }
        return true;
    }
    
                
    final private float getScore(final Sentence sentence, final int[][] graph,
                                  final ArrayList[][][] cache) {
        final ArrayList<Integer> args = sentence.arg_indices;
        final ArrayList<Integer> prds = sentence.prd_indices;

        final ArrayList feature = new ArrayList<>();
        ArrayList first_ord_feature;
        
        for (int case_label=0; case_label<case_length; case_label++) {
            ArrayList[][] tmp_cache = cache[case_label];
            
            for (int prd_i=0; prd_i<prds.size(); ++prd_i) {
                final int prd_id = prds.get(prd_i);
                final int arg_i = args.get(graph[prd_i][case_label]);

                if (tmp_cache[prd_id][arg_i] != null) {
                    feature.addAll(tmp_cache[prd_id][arg_i]);
                }
                else {
                    first_ord_feature =
                            extractFeature(sentence, arg_i, prd_id, case_label);
                    tmp_cache[prd_id][arg_i] = first_ord_feature;
                    feature.addAll(first_ord_feature);
                }            
            }
        }
/*        
        if (case_length > 1) {
            for (int prd_i=0; prd_i<prds.size(); ++prd_i) {
                final int[] prd_graph = graph[prd_i];
                feature.addAll(extractFrameFeature(sentence, prd_graph, prd_i));            
            }
        }
*/        
        feature.addAll(extractSecondOrdFeature(sentence, graph));        
        return calcScore(feature);
    }
    
    final private float getHammingDistance(final int[][] o_graph,
                                             final int[][] graph) {
        float hamming = 0.0f;

        for (int i=0; i<graph.length; ++i) {
            final int[] tmp_o_graph = o_graph[i];
            final int[] tmp_graph = graph[i];

            for (int j=0; j<tmp_o_graph.length; ++j) {
                if (tmp_o_graph[j] != tmp_graph[j]) hamming += 1.0f;
            }
        }
        
        return hamming;
    }
    

    final private ArrayList<Integer> extractFeature(Sentence sentence,
                                                      int prd_id,
                                                      int arg_id,
                                                      int case_label){
        return perceptron.feature.extractFeature(sentence, prd_id, arg_id,
                                                 case_label);
    }
    
    final private ArrayList<Integer> extractSecondOrdFeature(Sentence sentence,
                                                                int[][] graph){
        return perceptron.feature.extractFeature(sentence, graph);
    }
    
    final private ArrayList<Integer> extractFrameFeature(Sentence sentence,
                                                            int[] graph,
                                                            int prd_i){
        return perceptron.feature.extractFeature(sentence, graph, prd_i);
    }

    
    final public ArrayList extractFeature(final Sentence sentence,
                                            final int[][] o_graph){
        final ArrayList feature = new ArrayList<>();
        
        final int sent_id = sentence.index;
        final ArrayList<Integer> args = sentence.arg_indices;
        final ArrayList<Integer> prds = sentence.prd_indices;
        final ArrayList[][][] cache = perceptron.cache_feats[sent_id];


        for (int case_label=0; case_label<case_length; case_label++) {
            final ArrayList[][] case_cache = cache[case_label];
            
            for (int prd_i=0; prd_i<o_graph.length; ++prd_i) {
                final int prd_id = prds.get(prd_i);            
                final int arg_i = args.get(o_graph[prd_i][case_label]);

                if (case_cache[prd_id][arg_i] != null)
                    feature.addAll(case_cache[prd_id][arg_i]);
                else
                    feature.addAll(extractFeature(sentence, arg_i, prd_id,
                                                  case_label));
            }
        }
/*        
        if (case_length > 1) {
            for (int prd_i=0; prd_i<prds.size(); ++prd_i) {
                final int[] prd_graph = o_graph[prd_i];
                feature.addAll(extractFrameFeature(sentence, prd_graph, prd_i));            
            }
        }
*/        
        
        feature.addAll(extractSecondOrdFeature(sentence, o_graph));
        
        return feature;
    }
    
    final private float calcScore(ArrayList feature){
        return this.perceptron.calcScore(feature);
    }
    
}

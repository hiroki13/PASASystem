
import java.util.ArrayList;

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

    public HillClimbingParser(int nCases, int nSents, int maxSentLen, int weightSize) {
        this.nCases = nCases;
        this.perceptron = new Perceptron(nCases, nSents, maxSentLen, weightSize);
    }

    public HillClimbingParser(int nCases) {
        this.nCases = nCases;
        this.perceptron = new Perceptron();
    }
    
    @Override
    final public int[][] decode(final Sentence sentence, final int restart) {
        final int sent_i = sentence.index;
        final int[][] o_graph = sentence.oracleGraph;
        final int args_length = sentence.argIndices.size();
        final int prds_length = sentence.prdIndices.size();
        final ArrayList[][][] cache = this.perceptron.cacheFeats[sent_i];

        float prev_best_score = -10000000.0f, best_score = -10000000.0f;
        int[][] best_graph = new int[prds_length][nCases];
        int best = -1, best_case_label = -1, best_prd = -1;

        for (int i=0; i<restart; ++i) {
            final int[][] graph = setInitGraph(sentence);

            while (true) {
                final int[][] prev_graph = copyGraph(graph);
                
                for (int prd_i=0; prd_i<prds_length; ++prd_i) {
                    for (int case_label=0; case_label<nCases; case_label++) {
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
    
    @Override
    final public int[][] decode(Sentence sent, int restart, boolean test) {
        this.tmpCache = new ArrayList[nCases][52][52];
        
        final int args_length = sent.argIndices.size();
        final int prds_length = sent.prdIndices.size();
        final ArrayList[][][] cache = this.tmpCache;

        float prev_best_score = -100000.0f;
        float best_score = -100000.0f;
        int[][] best_graph = new int[prds_length][nCases];
        int best = -1;
        int best_case_label = -1;        
        int best_prd = -1;

        for (int i=0; i<restart; ++i) {
            final int[][] graph = setInitGraph(sent);

            while (true) {
                final int[][] prev_graph = copyGraph(graph);
                
                for (int prd_i=0; prd_i<prds_length; ++prd_i) {
                    for (int case_label=0; case_label<nCases; case_label++) {
                        for (int arg_i=0; arg_i<args_length; ++arg_i) {                    
                            final int[][] tmp_graph = copyGraph(graph);
                            
                            tmp_graph[prd_i][case_label] = arg_i;
                            float score = getScore(sent, tmp_graph, cache);

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
    
    final public int[][] decode(final Sentence sentence, final boolean test,
                                 final ArrayList init_graph) {
        this.tmpCache = new ArrayList[nCases][52][52];
        
        final int args_length = sentence.argIndices.size();
        final int prds_length = sentence.prdIndices.size();
        final ArrayList[][][] cache = this.tmpCache;

        float best_score = -100000.0f;
        int best = -1, best_case_label = -1, best_prd = -1;
            
        int[][] graph = setInitGraph(sentence);

        while (true) {        
            int[][] prev_graph = copyGraph(graph);
                            
            for (int prd_i=0; prd_i<prds_length; ++prd_i) {            
                for (int case_label=0; case_label<nCases; case_label++) {                
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
    
    private int[][] setInitGraph(final Sentence sentence) {
        final int args_length = sentence.argIndices.size();
        final int prds_length = sentence.prdIndices.size();
        final int[][] graph = new int[prds_length][nCases];
        
        for (int prd_i=0; prd_i<prds_length; ++prd_i) {
            graph[prd_i] = genGraph(args_length);
        }
        
        return graph;
    }

    private int[] genGraph(final int args_length) {
        final int[] graph = new int[nCases];
        
        for (int case_label=0; case_label<nCases; ++case_label)
            graph[case_label] = rnd.nextInt(args_length);

        return graph;
    }
    
    private int[][] copyGraph(final int[][] graph) {
        final int prds_length = graph[0].length;
        final int[][] copied_graph = new int[graph.length][prds_length];

        for (int i=0; i<graph.length; ++i)
            for (int arg_i=0; arg_i<prds_length; arg_i++)
                copied_graph[i][arg_i] = graph[i][arg_i];

        return copied_graph;
    }
    
    private boolean isGraphMatch(final int[][] graph1, final int[][] graph2) {
        for (int i=0; i<graph1.length; ++i) {
            final int[] tmp_graph1 = graph1[i];
            final int[] tmp_graph2 = graph2[i];
            
            for (int j=0; j<graph1[0].length; ++j)
                if (tmp_graph1[j] != tmp_graph2[j])
                    return false;
        }
        return true;
    }
    
                
    private float getScore(Sentence sentence, int[][] graph, ArrayList[][][] cache) {
        final ArrayList<Integer> args = sentence.argIndices;
        final ArrayList<Integer> prds = sentence.prdIndices;

        final ArrayList feature = new ArrayList<>();
        ArrayList first_ord_feature;
        
        for (int case_label=0; case_label<nCases; case_label++) {
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

        feature.addAll(extractSecondOrdFeature(sentence, graph));        
        return calcScore(feature);
    }
    
    private float getHammingDistance(int[][] o_graph, int[][] graph) {
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
    

    private ArrayList<Integer> extractFeature(Sentence sentence, int prd_id, int arg_id, int case_label){
        return perceptron.feature.getFeature(sentence, prd_id, arg_id, case_label);
    }
    
    private ArrayList<Integer> extractSecondOrdFeature(Sentence sentence, int[][] graph){
        return perceptron.feature.getFeature(sentence, graph);
    }
    
    
    @Override
    final public ArrayList getFeature(Sentence sentence, int[][] o_graph){
        final ArrayList feature = new ArrayList<>();
        
        final int sent_id = sentence.index;
        final ArrayList<Integer> args = sentence.argIndices;
        final ArrayList<Integer> prds = sentence.prdIndices;
        final ArrayList[][][] cache = perceptron.cacheFeats[sent_id];


        for (int case_label=0; case_label<nCases; case_label++) {
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
        
        feature.addAll(extractSecondOrdFeature(sentence, o_graph));        
        return feature;
    }
    
    private float calcScore(ArrayList feature) {
        return perceptron.calcScore(feature);
    }

}

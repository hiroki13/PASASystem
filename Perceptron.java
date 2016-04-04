/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author hiroki
 */

public class Perceptron implements Serializable{
    public float[] weight;
    public float[] aweight;
    public float t = 1.0f;
    public Feature feature;
    public ArrayList[][][][] cache_feats;
    public int sent_id = 0;
    public float total;
    public float correct;

    public Perceptron(int case_length, int sent_num, int max_sent_len,
                        int weight_len){
        this.weight = new float[weight_len];
        this.aweight = new float[weight_len];
        this.feature = new Feature(case_length);
        this.feature.w = weight.length;
        this.cache_feats =
                new ArrayList[sent_num][case_length][max_sent_len][max_sent_len];
    }
    
    public Perceptron() {
        
    }

    final public float calcScore(ArrayList<Integer> usedFeatures) {
        float score = 0.0f;
        int phi_id;
        
        for(int i=0; i<usedFeatures.size(); ++i) {
            phi_id = usedFeatures.get(i);
            score += this.weight[phi_id];
        }
        
        return score;
    }
    
    final public void updateWeights(ArrayList<Integer> o_feature,
                                      ArrayList<Integer> feature) {
        for (int i=0; i<o_feature.size(); ++i) {
            int phi_id = o_feature.get(i);
            this.weight[phi_id] += 1.0f;
            this.aweight[phi_id] += this.t;
        }
        
        for (int i=0; i<feature.size(); ++i) {
            int phi_id = feature.get(i);
            this.weight[phi_id] -= 1.0f;
            this.aweight[phi_id] -= this.t;
        }
        
        this.t += 1.0f;
    }

    final public void checkAccuracy(final int[][] o_graph, final int[][] graph) {
        for (int i=0; i<o_graph.length; ++i) {
            final int[] t_graph1 = o_graph[i];
            final int[] t_graph2 = graph[i];

            for (int j=0; j<t_graph1.length; ++j) {
                if (t_graph1[j] == t_graph2[j]) correct += 1.0f;
                total += 1.0f;
            } 
        }
    }
    
}

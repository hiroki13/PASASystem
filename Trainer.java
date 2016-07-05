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

final public class Trainer {
    public ArrayList<Sentence> sentencelist;
    public Parser parser;
    public Perceptron perceptron;
    public int n_cases;
    public ArrayList[] o_feature;

    Trainer(final ArrayList<Sentence> sentencelist, final int n_cases,
             final int max_sent_len, final int weight_len) {
        this.sentencelist = sentencelist;
        this.n_cases = n_cases;
        this.parser = new Parser(n_cases, sentencelist.size(),
                                 max_sent_len, weight_len);
        this.o_feature = new ArrayList[sentencelist.size()];
    }
    
    final public void train(int restart) {
        int counter = 0;
        this.parser.perceptron.sent_id = 0;
                
        for(int i=0; i<this.sentencelist.size(); ++i){
            Sentence sentence = this.sentencelist.get(i);

            if (!sentence.hasPrds) {
                if (counter%1000 == 0 && counter != 0)
                    System.out.print(String.format("%d ", counter));
                this.parser.perceptron.sent_id++;
                counter += 1;
                continue;
            }
            
            int args_length = sentence.argIndices.size();
            int prds_length = sentence.prdIndices.size();
            this.parser.perceptron.feature.cache =
                    new ArrayList[n_cases][n_cases][prds_length]
                                 [args_length][prds_length][args_length];
            
            ArrayList o_feature = this.o_feature[i];
            if (o_feature == null) {
                o_feature = parser.extractFeature(sentence, sentence.oracleGraph);
                this.o_feature[i] = o_feature;
            }
            
            final int[][] graph;
            graph = parser.decode(sentence, restart);

            final ArrayList feature = this.parser.extractFeature(sentence, graph);            
            this.parser.perceptron.updateWeights(o_feature, feature);
            this.parser.perceptron.checkAccuracy(sentence.oracleGraph, graph);

            if (counter%1000 == 0 && counter != 0)
                System.out.print(String.format("%d ", counter));

            this.parser.perceptron.sent_id++;
            counter += 1;
        }
    }
     
}

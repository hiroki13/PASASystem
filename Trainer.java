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
    final public ArrayList<Sentence> sents;
    final public Parser parser;
    final public int nCases;
    final public ArrayList[] oracleFeature;

    Trainer(String parser, ArrayList<Sentence> corpus, int nCases, int maxSentLen, int weightSize) {
        this.sents = corpus;
        this.nCases = nCases;
        if ("baseline".equals(parser))
            this.parser = new BaselineParser(nCases, corpus.size(), maxSentLen, weightSize);
        else
            this.parser = new HillClimbingParser(nCases, corpus.size(), maxSentLen, weightSize);
        this.oracleFeature = new ArrayList[corpus.size()];
    }
    
    final public void train() {
        int counter = 0;
        int crr = 0;
        parser.perceptron.sentIndex = 0;
                
        for(int i=0; i<sents.size(); ++i){
            Sentence sent = sents.get(i);

            if (!sent.hasPrds) {
                if (counter%1000 == 0 && counter != 0)
                    System.out.print(String.format("%d ", counter));
                parser.perceptron.sentIndex++;
                counter += 1;
                continue;
            }
            
            ArrayList oraclePhi = parser.getFeature(sent, sent.oracleGraph);
            int[][] systemGraph = parser.decode(sent);
            parser.perceptron.updateWeights(oraclePhi, parser.bestPhi);
            parser.perceptron.checkAccuracy(sent.oracleGraph, systemGraph);
            crr += getCorrects(sent.oracleGraph, systemGraph);

            if (counter%1000 == 0 && counter != 0)
                System.out.print(String.format("%d ", counter));

            parser.perceptron.sentIndex++;
            counter += 1;
        }
        System.out.print(String.format("%d ", crr));

    }
    
    final public int getCorrects(int[][] oracleGraph, int[][] systemGraph) {
        int crr = 0;
        for (int i=0; i<oracleGraph.length; ++i) {
            int[] oracleGraph_i = oracleGraph[i];
            int[] systemGraph_i = systemGraph[i];
            for (int j=0; j<oracleGraph_i.length; ++j) {
                int oracleArgIndex = oracleGraph_i[j];
                int systemArgIndex = systemGraph_i[j];
                if (oracleArgIndex == systemArgIndex) crr++;
            }
        }
        return crr;
    }

    final public void train(int restart) {
        int counter = 0;
        this.parser.perceptron.sentIndex = 0;
                
        for(int i=0; i<sents.size(); ++i){
            Sentence sent = sents.get(i);

            if (!sent.hasPrds) {
                if (counter%1000 == 0 && counter != 0)
                    System.out.print(String.format("%d ", counter));
                this.parser.perceptron.sentIndex++;
                counter += 1;
                continue;
            }
            
            int nArgs = sent.argIndices.size();
            int nPrds = sent.prdIndices.size();
            this.parser.perceptron.feature.cache =
                    new ArrayList[nCases][nCases][nPrds][nArgs][nPrds][nArgs];
            
            ArrayList o_feature = oracleFeature[i];
            if (o_feature == null) {
                o_feature = parser.getFeature(sent, sent.oracleGraph);
                oracleFeature[i] = o_feature;
            }
            
            final int[][] graph;
            graph = parser.decode(sent, restart);

            final ArrayList feature = parser.getFeature(sent, graph);            
            this.parser.perceptron.updateWeights(o_feature, feature);
            this.parser.perceptron.checkAccuracy(sent.oracleGraph, graph);

            if (counter%1000 == 0 && counter != 0)
                System.out.print(String.format("%d ", counter));

            this.parser.perceptron.sentIndex++;
            counter += 1;
        }
    }
     
}

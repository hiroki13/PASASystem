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

public class Trainer {

    final private int ITERATION;
    final private Preprocessor preprocessor;

    Trainer(int iteration) {
        ITERATION = iteration;
        preprocessor = new Preprocessor();
    }
    
    final public void train(Parser parser, ArrayList<Sentence> trainCorpus, ArrayList<Sentence> testCorpus) {
        System.out.println("\n\nTRAINING START");
        
        Sample[] trainSamples = createSamples(trainCorpus);
        Sample[] testSamples = createSamples(testCorpus);

        showStats(trainSamples);
        showStats(testSamples);

        parser.setOracleFeatIDs(trainSamples);

        for (int i=0; i<ITERATION; i++) {
            System.out.println(String.format("\n\nIteration %d: ", i+1));                
            long time1 = System.currentTimeMillis();
            trainEachEpoch(parser, trainSamples);
            long time2 = System.currentTimeMillis();
            System.out.println("Time: " + (time2-time1) + " ms\n");
            
            float[] weight = parser.perceptron.weight;
            float[] avgWeight = parser.perceptron.getAvgWeight();
            parser.perceptron.weight = avgWeight;
            predict(parser, testSamples);
            parser.perceptron.weight = weight;
        }
    }
    
    private void trainEachEpoch(Parser parser, Sample[] samples) {
        System.out.println("TRAIN SAMPLE");
        parser.evaluator = new Evaluator();

        for(int index=0; index<samples.length; ++index){
            if ((index+1) % 1000 == 0)
                System.out.print(String.format("%d ", index+1));

            Sample sample = samples[index];

            if (sample.prds.length == 0)
                continue;

            parser.train(sample);            
        }
        parser.evaluator.show();
    }
    
    private void predict(Parser parser, Sample[] samples) {
        System.out.println("TEST SAMPLE");
        parser.evaluator = new Evaluator();

        for(int index=0; index<samples.length; ++index){
            if ((index+1) % 1000 == 0)
                System.out.print(String.format("%d ", index+1));

            Sample sample = samples[index];

            if (sample.prds.length == 0)
                continue;

            int[][] bestGraph = parser.predict(sample);
            parser.evaluator.update(sample, sample.oracleGraph, bestGraph);
        }
        parser.evaluator.show();
        
    }

    private Sample[] createSamples(ArrayList<Sentence> corpus) {
        return preprocessor.createSamples(corpus);
    }
                 
    private void showStats(Sample[] samples) {
        preprocessor.showStats(samples);        
    }

}

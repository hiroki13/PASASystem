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

    final int ITERATION;
    final Preprocessor preprocessor;

    Trainer(int iteration, int nCases) {
        ITERATION = iteration;
        preprocessor = new Preprocessor(nCases);
    }
    
    final public void train(Parser parser,
                             ArrayList<Sentence> trainCorpus,
                             ArrayList<Sentence> testCorpus) {
        System.out.println("\n\nTRAINING START");
        
        Sample[] trainSamples = createSamples(trainCorpus);
        Sample[] testSamples = createSamples(testCorpus);

        setOracleFeatIDs(trainSamples, parser.featExtractor);
        for (int i=0; i<ITERATION; i++) {
            System.out.println(String.format("\nIteration %d: ", i+1));                
            long time1 = System.currentTimeMillis();
            trainEachEpoch(parser, trainSamples);
            long time2 = System.currentTimeMillis();
            System.out.println("\tTime: " + (time2-time1) + " ms");
        }
    }
    
    private void trainEachEpoch(Parser parser, Sample[] samples) {
        parser.evaluator = new Evaluator(parser.nCases);
        for(int index=0; index<samples.length; ++index){
            if ((index+1) % 100 == 0)
                System.out.print(String.format("%d ", index+1));

            Sample sample = samples[index];

            if (sample.prds.length == 0) {
                continue;
            }            
            parser.train(sample);            
        }
        parser.evaluator.showAccuracy();
    }

    private Sample[] createSamples(ArrayList<Sentence> corpus) {
        return preprocessor.createSamples(corpus);
    }
             
    private void setOracleFeatIDs(Sample[] samples, FeatureExtractor featExtractor) {
        for (int i=0; i<samples.length; ++i)
            samples[i].setOracleFeatIDs(featExtractor);
    }

}

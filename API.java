/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author hiroki
 */

public class API {
    
    final ParamChecker paramchecker;
    final Preprocessor preprocessor;

    final int N_CASES = 3;
    final String MODE;
    final String PARSER_TYPE;    
    final String TRAIN_FILE_PATH, TEST_FILE_PATH;
    final String OUTPUT_FILE_NAME, MODEL_FILE_NAME;
    final int ITERATION, RESTART, RND_SEED, WEIGHT_SIZE;

    ArrayList<Sentence> trainCorpus, testCorpus;    
    Perceptron perceptron;
    
    public API(String[] args) {
        paramchecker = new ParamChecker(args);
        preprocessor = new Preprocessor(N_CASES);

        MODE = paramchecker.getModeSelect();
        PARSER_TYPE = paramchecker.getParserType();

        TRAIN_FILE_PATH = paramchecker.getTrainFilePath(MODE);
        TEST_FILE_PATH = paramchecker.getTestFilePath(MODE);
        OUTPUT_FILE_NAME = paramchecker.getOutputFileName();
        MODEL_FILE_NAME = paramchecker.getModelFileName();
        
        ITERATION = paramchecker.getIteration();
        RESTART = paramchecker.getRestart();
        RND_SEED = paramchecker.getRndSeed();
        WEIGHT_SIZE = paramchecker.getWeightSize();
    }

    public void main() throws Exception{
        System.out.println("Cases to be analyzed: " + N_CASES);

        ArrayList<Sentence> trainCorpus = preprocessor.loadCorpus(TRAIN_FILE_PATH);
        ArrayList<Sentence> testCorpus = preprocessor.loadCorpus(TEST_FILE_PATH);

        if ("train".equals(MODE)) {
            train(trainCorpus, testCorpus);
        }
        else if ("test".equals(MODE)) {
            System.out.println("Test Sents: " + testCorpus.size());
            System.out.println("Model Loaded...");

            ObjectInputStream perceptronStream
                = new ObjectInputStream(new FileInputStream(MODEL_FILE_NAME));      
            perceptron = (Perceptron) perceptronStream.readObject();
            perceptronStream.close();

            System.out.println("Model Loading Completed\n");
            
            test();
        }
    }
    
    private void train(ArrayList<Sentence> trainCorpus, ArrayList<Sentence> testCorpus) {            
        System.out.println("Hill-Climbing Restart: " + RESTART);
        System.out.println("Initialization Seed: " + RND_SEED);
        System.out.println(String.format("Train Sents:%d  Test Sents: %d",
                                         trainCorpus.size(), testCorpus.size()));

        Trainer trainer = new Trainer(PARSER_TYPE, trainCorpus, N_CASES, maxSentLen, WEIGHT_SIZE);
                        
        System.out.println("TRAINING START");

        Evaluater checker = null;

        for (int i=0; i<ITERATION; i++) {
            System.out.println(String.format("\nIteration %d: ", i+1));
                
            long time1 = System.currentTimeMillis();
            trainer.train(RESTART);
            long time2 = System.currentTimeMillis();
            System.out.println("\tTime: " + (time2-time1) + " ms");
                
            if (i+1 < ITERATION) {                
                checker = new Evaluater(N_CASES);
                checker.test(testCorpus, trainer.parser, RESTART, true);
            }
            else if (i+1 == ITERATION) {
                checker = new Evaluater(N_CASES);
                checker.testAndOutput(testCorpus, trainer.parser, RESTART, OUTPUT_FILE_NAME, true, true);
            }
            
            trainer.parser.hasCache = true;
                
            if (i+1 == ITERATION) {
                double time = testCorpus.size()/(((double) checker.time)/1000.0);    
                System.out.println("\tTime: " + time + "sent./sec.");
                showPerformance(checker);
            } 
        }
    }
    
    private void test() throws IOException{
        Parser parser = new HillClimbingParser(N_CASES, RND_SEED);
        parser.perceptron = perceptron;

        System.out.println("TEST START");                
            
        for (int i=0; i<ITERATION; i++) {
            System.out.println(String.format("\nIteration %d: ", i+1));
            Evaluater checker = new Evaluater(N_CASES);

            String out_fn = OUTPUT_FILE_NAME + "-" + i;
            checker.testAndOutput(testCorpus, parser, RESTART, out_fn,
                                  false, false);
            
 
            double time = testCorpus.size()/(((double) checker.time)/1000.0);    
            System.out.println("\tTime: " + time + "sent./sec.");
            showPerformance(checker);
        }
    }
            
}

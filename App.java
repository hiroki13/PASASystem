/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

/**
 *
 * @author hiroki
 */

public class App {
    
    final Config config;
    final Preprocessor preprocessor;
    Perceptron perceptron;
    
    
    public App(String[] args) {
        config = new Config(args);
        preprocessor = new Preprocessor();
    }

    public void main() throws Exception{
        System.out.println("Cases to be analyzed: " + Config.N_CASES);

        ArrayList<Sentence> trainCorpus = preprocessor.loadCorpus(config.TRAIN_FILE_PATH);
        ArrayList<Sentence> testCorpus = preprocessor.loadCorpus(config.TEST_FILE_PATH);

        if ("train".equals(config.MODE)) {
            train(trainCorpus, testCorpus);
        }
        else if ("test".equals(config.MODE)) {
            System.out.println("Test Sents: " + testCorpus.size());
            System.out.println("Model Loaded...");

            ObjectInputStream perceptronStream
                = new ObjectInputStream(new FileInputStream(config.MODEL_FILE_NAME));      
            perceptron = (Perceptron) perceptronStream.readObject();
            perceptronStream.close();

            System.out.println("Model Loading Completed\n");
            
            test();
        }
    }
    
    private Parser selectParser() {
        if ("baseline".equals(config.PARSER_TYPE))
            return new BaselineParser(config.WEIGHT_SIZE);
        return new HillClimbingParser(config.WEIGHT_SIZE, config.RND_SEED);        
    }
    
    private void train(ArrayList<Sentence> trainCorpus, ArrayList<Sentence> testCorpus) {            
        System.out.println("Hill-Climbing Restart: " + config.RESTART);
        System.out.println("Initialization Seed: " + config.RND_SEED);
        System.out.println(String.format("Train Sents:%d  Test Sents: %d",
                                         trainCorpus.size(), testCorpus.size()));

        Parser parser = selectParser();
        Trainer trainer = new Trainer(config.ITERATION);
        trainer.train(parser, trainCorpus, testCorpus);

    }
    
    private void test() throws IOException{
        Parser parser = new HillClimbingParser(config.N_CASES, config.RESTART, config.RND_SEED);
        parser.perceptron = perceptron;

        System.out.println("TEST START");                
    }
            
}

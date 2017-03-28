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
    
    final private Config config;
    final private Preprocessor preprocessor;    
    
    public App(String[] args) {
        config = new Config(args);
        preprocessor = new Preprocessor();
    }

    public void main() throws Exception {
        System.out.println("Cases to be analyzed: " + Config.N_CASES);

        ArrayList<Sentence> trainCorpus = loadCorpus(config.TRAIN_FILE_PATH, config.DATA_SIZE);
        ArrayList<Sentence> testCorpus = loadCorpus(config.TEST_FILE_PATH, config.DATA_SIZE);

        switch (config.MODE) {
            case "train":
                train(trainCorpus, testCorpus);
                break;
            case "test":
                System.out.println("Test Sents: " + testCorpus.size());
                System.out.println("Model Loaded...");
                ObjectInputStream perceptronStream
                        = new ObjectInputStream(new FileInputStream(config.MODEL_FILE_NAME));
                Perceptron perceptron = (Perceptron) perceptronStream.readObject();
                perceptronStream.close();
                System.out.println("Model Loading Completed\n");
                test(perceptron);
                break;
        }
    }
    
    private Parser selectParser() {
        if ("baseline".equals(config.PARSER_TYPE))
            return new BaselineParser();
        return new HillClimbingParser(config.RESTART, config.RND_SEED);        
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
    
    private void test(Perceptron perceptron) throws IOException{
        Parser parser = new HillClimbingParser(config.RESTART, config.RND_SEED);
        parser.perceptron = perceptron;

        System.out.println("TEST START");                
    }
    
    private ArrayList<Sentence> loadCorpus(String fn, int dataSize) throws Exception {
        return preprocessor.loadCorpus(fn, dataSize);
    }
            
}

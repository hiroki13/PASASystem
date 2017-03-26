/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author hiroki
 */
public class Config {

    final OptionParser optionparser;
    final static int N_CASES = 3;
    final String MODE;
    final String PARSER_TYPE;    
    final String TRAIN_FILE_PATH, TEST_FILE_PATH;
    final String OUTPUT_FILE_NAME, MODEL_FILE_NAME;
    final int ITERATION, RESTART, RND_SEED, WEIGHT_SIZE;

    public Config(String[] args) {
        optionparser = new OptionParser(args);

        MODE = getModeSelect();
        PARSER_TYPE = getParserType();

        TRAIN_FILE_PATH = getTrainFilePath(MODE);
        TEST_FILE_PATH = getTestFilePath(MODE);
        OUTPUT_FILE_NAME = getOutputFileName();
        MODEL_FILE_NAME = getModelFileName();
        
        ITERATION = getIteration();
        RESTART = getRestart();
        RND_SEED = getRndSeed();
        WEIGHT_SIZE = getWeightSize();        
    }

    final public String getModeSelect() {
        String mode = optionparser.getString("mode");
        if (mode == null) {
            System.out.println("Enter -mode train/test");
            System.exit(0);
        }        
        return mode;        
    }
    
    final public String getTrainFilePath(String mode) {
        String train_fn = optionparser.getString("train");
        if ("train".equals(mode) && train_fn == null) {
            System.out.println("Enter -train filename");
            System.exit(0);
        }
        return train_fn;
    }

    final public String getTestFilePath(String mode) {
        String test_fn = optionparser.getString("test");
        if ("test".equals(mode) && test_fn == null) {
            System.out.println("Enter -test filename");
            System.exit(0);
        }
        return test_fn;
    }

    final public String getOutputFileName() {
        String fn = optionparser.getString("output");
        if (fn == null)
            fn = "output";
        return fn;
    }

    final public String getModelFileName() {
        String fn = optionparser.getString("model");
        if (fn == null)
            fn = "model";
        return fn;
    }

    final public String getParserType() {
        String fn = optionparser.getString("parser");
        if (fn == null)
            fn = "baseline";
        return fn;
    }
    
    final public int getIteration() {
        return optionparser.getInt("iter", 50);
    }

    final public int getRestart() {
        return optionparser.getInt("restart", 1);
    }

    final public int getRndSeed() {
        return optionparser.getInt("rnd", 0);
    }

    final public int getWeightSize() {
        return optionparser.getInt("weight", 100000);
    }

}

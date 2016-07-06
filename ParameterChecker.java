/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author hiroki
 */

final public class ParameterChecker {
    Mode mode;
    OptionParser optionparser;
    
    ParameterChecker(Mode mode) {
        this.mode = mode;
        this.optionparser = mode.optionparser;
    }

    private void checkParamArgs(){
        mode.train = optionparser.isExsist("train");
        mode.test = optionparser.isExsist("test");
        mode.output = optionparser.isExsist("output");
        mode.model = optionparser.isExsist("model");
        mode.check_accuracy = true;

        mode.parser = optionparser.getString("parser");

        mode.ga = optionparser.isExsist("ga");
        mode.o = optionparser.isExsist("o");
        mode.ni = optionparser.isExsist("ni");
        
        mode.iteration = optionparser.getInt("iter",10);
        mode.restart = optionparser.getInt("restart", 10);
        mode.rnd = optionparser.getInt("rnd", 0);
        mode.weightSize = optionparser.getInt("weight", 500000);
    }
    
    final public void setParams() {
        String modeselect = mode.modeselect;
        
        checkParamArgs();
        
        if ("train".equals(modeselect) || "train_word".equals(modeselect)) {
            setTrainFile();
            setTestFile();
            setOutputFile();
            setParsedCases(mode.ga, mode.o, mode.ni);
        }
        else if ("test".equals(modeselect)) {
            setTestFile();
            setModelFile();
            setOutputFile();
            setParsedCases(mode.ga, mode.o, mode.ni);
        }
        else System.out.println("Enter -mode train/test"); 
    }
    
    final public void setTrainFile() {
        if(mode.train) mode.trainFile = optionparser.getString("train");
        else {
            System.out.println("Enter -train filename");
            System.exit(0);
        }
    }
    
    final public void setTestFile() {
        if(mode.test) mode.testFile = optionparser.getString("test");
        else {
            System.out.println("Enter -test filename");
            System.exit(0);
        }
    }
        
    final public void setOutputFile() {
        if(mode.output) mode.outfile = optionparser.getString("output");
        else {
            System.out.println("Enter -output filename");
            System.exit(0);
        }
    }
    
    final public void setModelFile() {
        if(mode.model) mode.modelfile = optionparser.getString("model");
        else {
            System.out.println("Enter -model filename");
            System.exit(0);
        }        
    }

    private void setParsedCases(boolean ga, boolean o, boolean ni) {
        int[] case_labels;

        if (ga && o && ni) {
            case_labels = new int[3];
            case_labels[0] = 0;
            case_labels[1] = 1;
            case_labels[2] = 2;
        }
        else if (ga && o) {
            case_labels = new int[2];
            case_labels[0] = 0;
            case_labels[1] = 1;            
        }
        else if (ga && ni) {
            case_labels = new int[2];
            case_labels[0] = 0;
            case_labels[1] = 2;            
        }
        else if (o && ni) {
            case_labels = new int[2];
            case_labels[0] = 1;
            case_labels[1] = 2;            
        }
        else if (ga) {
            case_labels = new int[1];
            case_labels[0] = 0;            
        }
        else if (o) {
            case_labels = new int[1];
            case_labels[0] = 1;            
        }
        else if (ni){
            case_labels = new int[1];
            case_labels[0] = 2;            
        }
        else {
            case_labels = new int[3];
            case_labels[0] = 0;
            case_labels[1] = 1;
            case_labels[2] = 2;            
        }
        
        mode.caseLabels = case_labels;
        mode.nCases = case_labels.length;
    }
        
}

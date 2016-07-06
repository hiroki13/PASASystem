/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author hiroki
 */

final public class Mode {
    
    final OptionParser optionparser;
    String modeselect, parser;    
    String trainFile, testFile, outfile, modelfile;
    boolean train, test, output, model, check_accuracy;
    boolean ga, o, ni;
    int iteration, restart, rnd, nCases, maxSentLen, weightSize;
    int[] caseLabels;
    ArrayList<Sentence> trainSent, testSent;    
    Perceptron perceptron;
    
    Mode(String[] args) throws NoSuchAlgorithmException{
        optionparser = new OptionParser(args);
        boolean mode = optionparser.isExsist("mode");
        
        if (mode) modeselect = optionparser.getString("mode");
        else {
            System.out.println("Enter -mode train/test");
            System.exit(0);
        }
    }

    final public void main() throws Exception{
        ParameterChecker p_checker = new ParameterChecker(this);

        // Check input files
        p_checker.setParams();

        System.out.println("Cases to be analyzed: " + nCases);

        if ("train".equals(modeselect)) {
            Reader reader = new Reader();
            
            trainSent = reader.read(trainFile, nCases, caseLabels);
            testSent = reader.read(testFile, nCases, caseLabels);
            maxSentLen = reader.maxSentLen;

            System.out.println(String.format("Train Sents: %d\tTest Sents: %d\tMax Sent Length: %d",                        
                trainSent.size(), testSent.size(), maxSentLen));
            
            train();
        }
        else if ("train_word".equals(modeselect)) {
            Reader reader = new Reader();
            
            trainSent = reader.readWord(trainFile, nCases, caseLabels);
            testSent = reader.readWord(testFile, nCases, caseLabels);
            maxSentLen = reader.maxSentLen;

            System.out.println(String.format("Train Sents: %d\tTest Sents: %d\tMax Sent Length: %d",
                    trainSent.size(), testSent.size(), maxSentLen));
            showStatistics(trainSent);
            trainWord();
        }
        else if ("test".equals(modeselect)) {
            Reader reader = new Reader();

            testSent = reader.read(testFile, nCases, caseLabels);
            System.out.println("Test Sents: " + testSent.size());
            System.out.println("Model Loaded...");

            ObjectInputStream perceptronStream
                = new ObjectInputStream(new FileInputStream(modelfile));      
            perceptron = (Perceptron) perceptronStream.readObject();
            perceptronStream.close();

            System.out.println("Model Loading Completed\n");
            
            test();
        }
    }

    private void train() throws IOException{            
        System.out.println("Hill-Climbing Restart: " + restart);
        System.out.println("Initialization Seed: " + rnd);

        Trainer trainer = new Trainer(parser, trainSent, nCases, maxSentLen, weightSize);

        // Set random seed for ititial graphs
        if (rnd != 0) trainer.parser.rnd = new Random(rnd);
        else trainer.parser.rnd = new Random();
        
        trainer.parser.nCases = nCases;
                        
        System.out.println("TRAINING START");

        AccuracyChecker checker = null;

        for (int i=0; i<iteration; i++) {
            System.out.println(String.format("\nIteration %d: ", i+1));
                
            long time1 = System.currentTimeMillis();
            trainer.train(restart);
            long time2 = System.currentTimeMillis();
            System.out.println("\tTime: " + (time2-time1) + " ms");
                
            if (i+1<iteration && check_accuracy) {                
                checker = new AccuracyChecker(nCases);
                checker.test(testSent, trainer.parser, restart, true);
            }
            else if (i+1 == iteration) {
                checker = new AccuracyChecker(nCases);
                checker.testAndOutput(testSent, trainer.parser, restart, outfile, true, true);
            }
            
            trainer.parser.hasCache = true;
                
            if (check_accuracy || i+1 == iteration) {
                double time = testSent.size()/(((double) checker.time)/1000.0);    
                System.out.println("\tTime: " + time + "sent./sec.");
                showPerformance(checker);
            } 
        }
    }
    
    private void trainWord() throws IOException{            
        Trainer trainer = new Trainer(parser, trainSent, nCases, maxSentLen, weightSize);
        trainer.parser.nCases = nCases;
                        
        System.out.println("TRAINING START");

        AccuracyChecker checker = null;

        for (int i=0; i<iteration; i++) {
            System.out.println(String.format("\nIteration %d: ", i+1));
                
            long time1 = System.currentTimeMillis();
            trainer.train();
            long time2 = System.currentTimeMillis();
            System.out.println("\tTime: " + (time2-time1) + " ms");
                
            if (i+1<iteration && check_accuracy) {                
                checker = new AccuracyChecker(nCases);
                checker.test(testSent, trainer.parser, true);
            }
            else if (i+1 == iteration) {
                checker = new AccuracyChecker(nCases);
                checker.testAndOutput(testSent, trainer.parser, restart, outfile, true, true);
            }
            
            trainer.parser.hasCache = true;
                
            if (check_accuracy || i+1 == iteration) {
                double time = testSent.size()/(((double) checker.time)/1000.0);    
                System.out.println("\tTime: " + time + "sent./sec.");
                showPerformance(checker);
            } 
        }
    }
    
    private void test() throws IOException{
        Parser parser = new HillClimbingParser(nCases);
        parser.perceptron = perceptron;

        if (rnd != 0) parser.rnd = new Random(rnd);
        else parser.rnd = new Random();

        System.out.println("TEST START");                
            
        for (int i=0; i<iteration; i++) {
            System.out.println(String.format("\nIteration %d: ", i+1));
            AccuracyChecker checker = new AccuracyChecker(nCases);

            String out_fn = outfile + "-" + i;
            checker.testAndOutput(testSent, parser, restart, out_fn,
                                  false, false);
            
 
            double time = testSent.size()/(((double) checker.time)/1000.0);    
            System.out.println("\tTime: " + time + "sent./sec.");
            showPerformance(checker);
        }
    }
    
    private void showStatistics(ArrayList<Sentence> corpus) {
        int[][] ttl_cases = new int[][]{{0, 0, 0}, {0, 0, 0}, {0, 0, 0}};
        for (int i=0; i<corpus.size(); ++i) {
            Sentence sent = corpus.get(i);
            for (int j=0; j<sent.caseStatistics.length; ++j) {
                int[] cases = sent.caseStatistics[j];
                for (int k=0; k<cases.length; ++k)
                    ttl_cases[j][k] += cases[k];
            }
        }
        for (int i=0; i<ttl_cases.length; ++i)
            System.out.println(String.format("\tCase:%d  Dep: %d  Zero: %d Inter: %d",
                    i, ttl_cases[i][0], ttl_cases[i][1], ttl_cases[i][2]));
    }

    final public void showPerformance(AccuracyChecker checker) {
        for (int case_label=0; case_label<nCases; case_label++)
            setCasePerformance(checker, case_label);
        setAllPerformance(checker);
    }
    
    private void setCasePerformance(AccuracyChecker checker, int case_label) {
        float r_dep = checker.r_dep[case_label];
        float r_total = checker.r_dep[case_label] + checker.r_zero[case_label];
        
        float p_dep = checker.p_total[case_label] - checker.p_zero[case_label];
        float correct_dep = checker.correct[case_label] -
                            checker.correct_zero[case_label];
                
        float recall = checker.correct[case_label] / r_total;
        float precision = checker.correct[case_label] /
                          checker.p_total[case_label];
        float f1 = (2*recall*precision) / (recall+precision);
                
        float recall_dep = correct_dep / r_dep;
        float precision_dep = correct_dep / p_dep;
        float f1_dep = (2*recall_dep*precision_dep) /
                       (recall_dep+precision_dep);

        float recall_zero = checker.correct_zero[case_label] /
                            checker.r_zero[case_label];
        float precision_zero = checker.correct_zero[case_label] /
                               checker.p_zero[case_label];
        float f1_zero = (2*recall_zero*precision_zero) /
                        (recall_zero+precision_zero);

        
        System.out.println(String.format
            ("\n\tALL Precision %f Recall %f F1 %f CORRECT %d P_TOTAL %d R_TOTAL %d",
            precision, recall, f1, (int) checker.correct[case_label],
            (int) checker.p_total[case_label], (int) r_total));
        System.out.println(String.format
            ("\tDEP Precision %f Recall %f F1 %f CORRECT %d P_TOTAL %d R_TOTAL %d",
            precision_dep, recall_dep, f1_dep, (int) correct_dep,
            (int) p_dep, (int) r_dep));
        System.out.println(String.format
            ("\tZERO Precision %f Recall %f F1 %f CORRECT %d P_TOTAL %d R_TOTAL %d",
            precision_zero, recall_zero, f1_zero,
            (int) checker.correct_zero[case_label],
            (int) checker.p_zero[case_label],
            (int) checker.r_zero[case_label]));    
    }

    private void setAllPerformance(AccuracyChecker checker) {
        float r_total = 0.0f;
        float r_dep = 0.0f;
        float r_zero = 0.0f;
        float p_total = 0.0f;
        float p_dep = 0.0f;
        float p_zero = 0.0f;
        float correct_total = 0.0f;
        float correct_dep = 0.0f;
        float correct_zero = 0.0f;
        
        for (int case_label=0; case_label<nCases; case_label++) {
            r_total += checker.r_dep[case_label] + checker.r_zero[case_label];
            r_dep += checker.r_dep[case_label];
            r_zero += checker.r_zero[case_label];
        
            p_total += checker.p_total[case_label];
            p_dep += checker.p_total[case_label] - checker.p_zero[case_label];
            p_zero += checker.p_zero[case_label];

            correct_total += checker.correct[case_label];
            correct_dep += checker.correct[case_label] - checker.correct_zero[case_label];
            correct_zero += checker.correct_zero[case_label];
        }

        float precision_total = correct_total / p_total;
        float recall_total = correct_total / r_total;
        float f_total = (2*precision_total*recall_total) / (precision_total+recall_total);
        
        float precision_dep = correct_dep / p_dep;
        float recall_dep = correct_dep / r_dep;
        float f_dep = (2*precision_dep*recall_dep) / (precision_dep+recall_dep);

        float precision_zero = correct_zero / p_zero;
        float recall_zero = correct_zero / r_zero;
        float f_zero = (2*precision_zero*recall_zero) / (precision_zero+recall_zero);
        
        System.out.println("\nTOTAL RESULTS");
        System.out.println(String.format
            ("\n\tALL Precision %f Recall %f F1 %f CORRECT %d P_TOTAL %d R_TOTAL %d",
            precision_total, recall_total, f_total, (int) correct_total,
            (int) p_total, (int) r_total));
        System.out.println(String.format
            ("\tDEP Precision %f Recall %f F1 %f CORRECT %d P_TOTAL %d R_TOTAL %d",
            precision_dep, recall_dep, f_dep, (int) correct_dep, (int) p_dep, (int) r_dep));
        System.out.println(String.format
            ("\tZERO Precision %f Recall %f F1 %f CORRECT %d P_TOTAL %d R_TOTAL %d",
            precision_zero, recall_zero, f_zero, (int) correct_zero, (int) p_zero, (int) r_zero));    
    }
        
}

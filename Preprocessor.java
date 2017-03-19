
import java.util.ArrayList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author hiroki
 */
public class Preprocessor {

    final Reader reader;
    final int N_CASES;

    public Preprocessor(int nCases) {
        reader = new Reader(nCases);
        N_CASES = nCases;
    }

    public ArrayList<Sentence> loadCorpus(String fn) throws Exception {
        ArrayList<Sentence> corpus = reader.read(fn);
        showStatistics(corpus);
        return corpus;
    }
    
    public Sample[] createSamples(ArrayList<Sentence> corpus) {
        int nSamples = corpus.size();
        Sample[] samples = new Sample[nSamples];
        for (int i=0; i<nSamples; ++i)
            samples[i] = createSample(corpus.get(i));
        return samples;
    }

    private Sample createSample(Sentence sent) {
        return new Sample(sent);
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
        System.out.println();
    }

    final public void showPerformance(Evaluator checker) {
        for (int case_label=0; case_label<N_CASES; case_label++)
            setCasePerformance(checker, case_label);
        setAllPerformance(checker);
    }
    
    private void setCasePerformance(Evaluator checker, int case_label) {
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

        
        System.out.println(String.format("\n\tCase: %d", case_label));
        System.out.println(String.format
            ("\tALL Precision %f Recall %f F1 %f CORRECT %d P_TOTAL %d R_TOTAL %d",
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

    private void setAllPerformance(Evaluator checker) {
        float r_total = 0.0f;
        float r_dep = 0.0f;
        float r_zero = 0.0f;
        float p_total = 0.0f;
        float p_dep = 0.0f;
        float p_zero = 0.0f;
        float correct_total = 0.0f;
        float correct_dep = 0.0f;
        float correct_zero = 0.0f;
        
        for (int case_label=0; case_label<N_CASES; case_label++) {
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

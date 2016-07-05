/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author hiroki
 */

public class Sentence implements Serializable{
    public int index;  // index within the dataset
    public int ntcId;  // predefined id in NAIST Text Corpus

    public ArrayList<Word> words = new ArrayList();
    public ArrayList<Chunk> chunks = new ArrayList();

    // argIndces contains only the index of the chunk that has a head word
    public ArrayList<Integer> argIndices = new ArrayList<>();
    public ArrayList<Integer> prdIndices = new ArrayList<>();
    
    // oracleGraph contains the arg_indices: 1D: nPrds, 2D: nCases
    public int[][] oracleGraph;
    
    // whether this sentence contains any preds
    public boolean hasPrds;

    // total number of dep/zero case args
    public float[] nDepCaseArgs, nZeroCaseArgs;

    // dep info used for features
    int[][] depDist;
    String[][] depPath, depPosPath, depVerbPath, depRformPath, depAuxPath, depJoshiPath;

    // number of cases to be parsed
    public int nCases;
    
    // number of each case type
    public int[][] caseStatistics;
    
    public Sentence() {}

    public int size() {return -1;}

    public void add(Word token) {}

    public void add(Chunk chunk) {}

    public boolean hasDepCicle() {return false;}
    
    public void setElemParams() {}

    public void setParams(int[] caseLabels) {}

    public void setParams() {}
    
    public void setCaseStatistics() {}

}

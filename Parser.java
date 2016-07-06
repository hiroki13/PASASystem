/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author hiroki
 */

public class Parser {
    public Perceptron perceptron;
    public boolean hasCache = false;
    public ArrayList[][][] tmpCache;
    public ArrayList<Integer> bestPhi;
    public Random rnd;
    public int nCases;

    public Parser() {}

    public Parser(int nCases) {}

    public int[][] decode(Sentence sentence) {
        return new int[][]{};
    }

    public int[][] decode(Sentence sentence, int restart) {
        return new int[][]{};
    }

    public int[][] decode(Sentence sent, int restart, boolean test) {
        return new int[][]{};
    }

    public ArrayList getFeature(Sentence sentence, int[][] oracleGraph){
        return new ArrayList();
    }

}

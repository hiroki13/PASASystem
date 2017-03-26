
import java.util.ArrayList;
import java.util.Random;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author hiroki
 */

public class HillClimbingParser extends Parser {
    
    final private int RESTART;
    final private Random rnd;

    public HillClimbingParser(int weightSize, int restart, int rndSeed) {
        this.rnd = setRndSeed(rndSeed);
        this.RESTART = restart;
        this.perceptron = new Perceptron(weightSize);
    }

    public HillClimbingParser(int restart, int rndSeed) {
        this.rnd = setRndSeed(rndSeed); 
        this.RESTART = restart;
        this.perceptron = new Perceptron();
    }
    
    private Random setRndSeed(int rndSeed) {
        if (rndSeed == 0)
            return new Random();        
        return new Random(rndSeed);
    }
    
}

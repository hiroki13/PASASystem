
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

public class BaselineParser extends Parser{

    public BaselineParser(int nCases, int nSents, int maxSentLen, int weightSize, int rndSeed) {
        this.nCases = nCases;
        if (rndSeed != 0)
            this.rnd = new Random(rndSeed);
        else
            this.rnd = new Random();        
        this.perceptron = new Perceptron(nCases, nSents, maxSentLen, weightSize);
    }

    public BaselineParser(int nCases, int rndSeed) {
        this.nCases = nCases;
        if (rndSeed != 0)
            this.rnd = new Random(rndSeed);
        else
            this.rnd = new Random();        
        this.perceptron = new Perceptron();
    }

    @Override
    final public int[][] decode(Sentence sent) {
        ArrayList<Integer> argIndices = sent.argIndices;
        ArrayList<Integer> prdIndices = sent.prdIndices;
        int nArgs = argIndices.size();
        int nPrds = prdIndices.size();

        int[][] bestGraph = new int[nPrds][nCases];
        bestPhi = new ArrayList();

        for (int prd_i=0; prd_i<nPrds; ++prd_i) {            
            int prdIndex = prdIndices.get(prd_i);            

            for (int caseLabel=0; caseLabel<nCases; caseLabel++) {        
                int bestArgIndex = -1;
                float bestScore = -10000000.0f;
                ArrayList tmpBestPhi = null;

                for (int argIndex=0; argIndex<nArgs; ++argIndex) {
                    ArrayList phi = getFeature(sent, prdIndex, argIndex, caseLabel);
                    float score = getScore(phi);

                    if (score > bestScore) {
                        bestScore = score;
                        bestArgIndex = argIndex;
                        tmpBestPhi = phi;
                    }
                }

                bestGraph[prd_i][caseLabel] = bestArgIndex;
                bestPhi.addAll(tmpBestPhi);
            }
        }
                
        return bestGraph;
    }
    
    private float getScore(ArrayList Feature) {
        return perceptron.calcScore(Feature);
    }
        
    @Override
    final public ArrayList getFeature(Sentence sent, int[][] graph){
        final ArrayList phi = new ArrayList();
        
        final ArrayList<Integer> prdIndices = sent.prdIndices;
        final ArrayList[][][] cache = perceptron.cacheFeats[sent.index];


        for (int caseLabel=0; caseLabel<nCases; caseLabel++) {
            ArrayList[][] caseCache = cache[caseLabel];
            
            for (int prd_i=0; prd_i<graph.length; ++prd_i) {
                int prdIndex = prdIndices.get(prd_i);            
                int argIndex = graph[prd_i][caseLabel];

                if (caseCache[prdIndex][argIndex] != null)
                    phi.addAll(caseCache[prdIndex][argIndex]);
                else
                    phi.addAll(getFeature(sent, prdIndex, argIndex, caseLabel));
            }
        }
        
        return phi;
    }

    private ArrayList<Integer> getFeature(Sentence sentence, int prdIndex, int argIndex, int caseLabel){
        return perceptron.feature.getWordFeature(sentence, prdIndex, argIndex, caseLabel);
    }

}

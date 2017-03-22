
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

    public BaselineParser() {
        this.perceptron = new Perceptron();
        this.featExtractor = new FeatureExtractor();
    }

    public BaselineParser(int weightSize) {
        this.perceptron = new Perceptron(weightSize);
        this.featExtractor = new FeatureExtractor();
    }

    @Override
    final public Graph decode(Sample sample) {
        int nPrds = sample.prds.length;
        int nArgs = sample.args.length;
        Graph graph = new Graph(nPrds, nArgs, nCases);

        for (int prdIndex=0; prdIndex<nPrds; ++prdIndex) {
            Chunk prd = sample.prds[prdIndex];

            for (int argIndex=0; argIndex<nArgs; ++argIndex) {
                Chunk arg = sample.args[argIndex];                

                for (int caseLabel=0; caseLabel<nCases; caseLabel++) {
                    int [] labeledFeatIDs = extractLabeledFeatIDs(sample, prd, arg, caseLabel);
                    float score = calcScore(labeledFeatIDs);                    
                    graph.addFeatIDs(labeledFeatIDs, prdIndex, argIndex, caseLabel);
                    graph.addScore(score, prdIndex, argIndex, caseLabel);
                }
            }
        }
                
        return graph;
    }
            
}


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
    final int N_CASES = Config.N_CASES;

    public Preprocessor() {
        reader = new Reader();
    }

    public ArrayList<Sentence> loadCorpus(String fn) throws Exception {
        return reader.read(fn);
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
    
    final public void showStats(Sample[] samples) {
        int[][] caseCount = new int[][]{{0, 0, 0}, {0, 0, 0}, {0, 0, 0}};
        int nPrds = 0;

        for (int sampleIndex=0; sampleIndex<samples.length; ++sampleIndex) {
            Sample sample = samples[sampleIndex];
            Chunk[] chunkPrds = sample.prds;
            nPrds += chunkPrds.length;
            
            for (int prdIndex=0; prdIndex<chunkPrds.length; ++prdIndex) {
                Word prd = chunkPrds[prdIndex].prd;

                for (int caseLabel=0; caseLabel<N_CASES; ++caseLabel)
                    for (int argType=0; argType<Word.INTER_ZERO; ++argType)
                        if (prd.argIndices[caseLabel][argType] > -1) {
                            caseCount[caseLabel][argType] += 1;
                            break;
                        }
            }
        }

        System.out.println(String.format("\tPrds: %d", nPrds));
        for (int i=0; i<caseCount.length; ++i)
            System.out.println(String.format("\tCase:%d  Dep: %d  Zero: %d Inter: %d",
                    i, caseCount[i][0], caseCount[i][1], caseCount[i][2]));
        System.out.println();
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.Serializable;

/**
 *
 * @author hiroki
 */

public class FeatureExtractor implements Serializable{
    final public int nCases = Config.N_CASES;
//    final public static int SIZE = (int) Math.pow(2, 23);
    public static int SIZE = 10000;
    final public static int N_FEATS = 7;
    
    public FeatureExtractor() {}
    
    final public int[] extractUnlabeledFeatIDs(Sample sample, Chunk prd, Chunk arg) {
        Chunk argNext = Sample.getNextChunk(sample, arg);
        Word ph = prd.chead;
        Word ah = arg.chead;

        int[] featIDs = {
                Template.genUnlabeled(Template.PRD_RFORM.hash,    ph.REG.hashCode()),
                Template.genUnlabeled(Template.PRD_CPOS.hash,    ph.CPOS.hashCode()),
                Template.genUnlabeled(Template.PRD_POS.hash,    ph.POS.hashCode()),
                Template.genUnlabeled(Template.ARG_RFORM.hash,    ah.REG.hashCode()),
                Template.genUnlabeled(Template.ARG_CPOS.hash,    ah.CPOS.hashCode()),
                Template.genUnlabeled(Template.ARG_POS.hash,    ah.POS.hashCode()),
        };

        return featIDs;
    }

    final public int[] extractLabeledFeatIDs(Sample sample, Chunk prd, Chunk arg, int caseLabel) {
        Chunk argNext = Sample.getNextChunk(sample, arg);
        Word ph = prd.chead;
        Word ah = arg.chead;

        int[] featIDs = {
                Template.gen(Template.PRD_RFORM.hash,    ph.REG.hashCode(), caseLabel),
                Template.gen(Template.PRD_CPOS.hash,    ph.CPOS.hashCode(),    caseLabel),
                Template.gen(Template.PRD_POS.hash,    ph.POS.hashCode(),    caseLabel),
                Template.gen(Template.ARG_RFORM.hash,    ah.REG.hashCode(), caseLabel),
                Template.gen(Template.ARG_CPOS.hash,    ah.CPOS.hashCode(), caseLabel),
                Template.gen(Template.ARG_POS.hash,    ah.POS.hashCode(),   caseLabel),
                Template.gen(Template.BI_RFORM.hash,    ph.REG.hashCode(),    ah.REG.hashCode(),   caseLabel),
        };

        return featIDs;
    }
    
    final public int[] extractOracleFeatIDs(Sample sample) {
        int[] featIDs = new int[sample.prds.length * nCases * N_FEATS];

        for (int prdIndex=0; prdIndex<sample.prds.length; ++prdIndex) {
            Chunk prd = sample.prds[prdIndex];
            int[] argIndices = sample.oracleGraph[prdIndex];
            
            for (int caseLabel=0; caseLabel<argIndices.length; ++caseLabel) {
                Chunk arg = sample.args[argIndices[caseLabel]];
                int[] tmpFeatIDs = extractLabeledFeatIDs(sample, prd, arg, caseLabel);
                System.arraycopy(tmpFeatIDs, 0, featIDs, (prdIndex*nCases+caseLabel)*N_FEATS, N_FEATS);
            }
        }

        return featIDs;
    }

    private enum Template {
        // Unigrams
        PRD_RFORM("p:rform"),
        PRD_CPOS("p:cpos"),
        PRD_POS("p:pos"),
        ARG_RFORM("a:rform"),
        ARG_CPOS("a:cpos"),
        ARG_POS("a:pos"),

        BI_RFORM("bi:rform");

        private final String label;
        private final int hash;

        Template(String label) {
            this.label = label;
            this.hash = label.hashCode();
        }

        private static int gen(int... key) {
            int hash = oneAtATimeHashAll(key);
            return Math.abs(hash) % SIZE;
//            return (hash >>> 1) % SIZE;
        }

        private static int genUnlabeled(int... key) {
            return oneAtATimeHashUnlabeled(key);
        }

        private static int genLabeled(int featID, int caseLabel) {
            int hash = oneAtATimeHashLabeled(featID, caseLabel);
            return Math.abs(hash) % SIZE;
        }
    }

    private static int oneAtATimeHashAll(int[] key) {
        int hash = 0;
        for (int v : key) {
            hash += v;
            hash += (hash << 10);
            hash ^= (hash >> 6);
        }
        hash += (hash << 3);
        hash ^= (hash >> 11);
        hash += (hash << 15);
        return hash;
    }

    private static int oneAtATimeHashUnlabeled(int[] key) {
        int hash = 0;
        for (int v : key) {
            hash += v;
            hash += (hash << 10);
            hash ^= (hash >> 6);
        }
        return hash;
    }

    private static int oneAtATimeHashLabeled(int hash, int caseLabel) {
        hash += caseLabel;
        hash += (hash << 10);
        hash ^= (hash >> 6);
        hash += (hash << 3);
        hash ^= (hash >> 11);
        hash += (hash << 15);
        return hash;
    }
    
}

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
    public static int SIZE = 100000;
    final public static int N_FEATS = 42;
    
    public FeatureExtractor() {}
    
    final public int[] extractLabeledFeatIDs(Sample sample, Chunk prd, Chunk arg, int prdIndex, int caseLabel) {
        Chunk arg2 = Sample.getNextChunk(sample, arg);
        Word ph = prd.chead;
        Word ah = arg.chead;
        Word ah2 = arg2.chead;
        
        int depDist = sample.depDist[prdIndex][arg.INDEX];
        String depPath = sample.depPath[prdIndex][arg.INDEX];
        String depPosPath = sample.depPosPath[prdIndex][arg.INDEX];
        String depVerbPath = sample.depVerbPath[prdIndex][arg.INDEX];
        String depRformPath = sample.depRformPath[prdIndex][arg.INDEX];

        int[] featIDs = {
                Template.gen(Template.PRD_FORM.hash,    ph.FORM.hashCode(), caseLabel),
                Template.gen(Template.ARG_FORM.hash,    ah.FORM.hashCode(), caseLabel),
                Template.gen(Template.ARG2_FORM.hash,   ah2.FORM.hashCode(), caseLabel),

                Template.gen(Template.PRD_CPOS.hash,    ph.CPOS.hashCode(),    caseLabel),
                Template.gen(Template.ARG_CPOS.hash,    ah.CPOS.hashCode(),    caseLabel),
                Template.gen(Template.ARG2_CPOS.hash,    ah2.CPOS.hashCode(),    caseLabel),

                Template.gen(Template.PRD_POS.hash,    ph.POS.hashCode(),    caseLabel),
                Template.gen(Template.ARG_POS.hash,    ah.POS.hashCode(),    caseLabel),
                Template.gen(Template.ARG2_POS.hash,    ah2.POS.hashCode(),    caseLabel),

                Template.gen(Template.PRD_RFORM.hash,    prd.regform.hashCode(), caseLabel),
                Template.gen(Template.ARG_RFORM.hash,    arg.regform.hashCode(), caseLabel),
                Template.gen(Template.ARG2_RFORM.hash,   arg2.regform.hashCode(), caseLabel),

                Template.gen(Template.PRD_TYPE.hash,    ph.TYPE.hashCode(),    caseLabel),
                Template.gen(Template.ARG_TYPE.hash,    ah.TYPE.hashCode(),    caseLabel),

                Template.gen(Template.PRD_INFL.hash,    ph.INFL.hashCode(),    caseLabel),
                Template.gen(Template.ARG_INFL.hash,    ah.INFL.hashCode(),    caseLabel),

                Template.gen(Template.ARG_PARTICLE.hash,    arg.particle.hashCode(),    caseLabel),
                Template.gen(Template.ARG_COMPCONT.hash,    arg.compoundContWord.hashCode(),    caseLabel),
                Template.gen(Template.ARG_COMPFUNC.hash,    arg.compoundFuncWord.hashCode(),    caseLabel),

                Template.gen(Template.PRD_SAHEN.hash,    prd.sahenNoun.hashCode(),    caseLabel),
                Template.gen(Template.PRD_VOICE.hash,    prd.voiceSuffix.hashCode(),    caseLabel),

                Template.gen(Template.BI_RFORM_FORM.hash,    prd.regform.hashCode(),    ah.FORM.hashCode(),   caseLabel),
                Template.gen(Template.BI_RFORM_CCW.hash,    prd.regform.hashCode(),    arg.compoundContWord.hashCode(),   caseLabel),
                Template.gen(Template.BI_SAHEN_FORM.hash,    prd.sahenNoun.hashCode(),    ah.FORM.hashCode(),   caseLabel),

                Template.gen(Template.BI_RFORM_CFW.hash,    prd.regform.hashCode(),    arg.compoundFuncWord.hashCode(),   caseLabel),
                Template.gen(Template.BI_SAHEN_CFW.hash,    prd.sahenNoun.hashCode(),    arg.compoundFuncWord.hashCode(),   caseLabel),
                Template.gen(Template.BI_VOICE_CFW.hash,    prd.voiceSuffix.hashCode(),    arg.compoundFuncWord.hashCode(),   caseLabel),
                Template.gen(Template.BI_SAHEN_VOICE_CFW.hash,    prd.sahenNoun.hashCode(),  prd.voiceSuffix.hashCode(), arg.compoundFuncWord.hashCode(),   caseLabel),

                Template.gen(Template.DEP_DIST.hash, depDist,   caseLabel),
                Template.gen(Template.DEP_PATH.hash, depPath.hashCode(),   caseLabel),
                Template.gen(Template.DEP_POSPATH.hash, depPosPath.hashCode(),   caseLabel),
                Template.gen(Template.DEP_VERBPATH.hash, depVerbPath.hashCode(),   caseLabel),
                Template.gen(Template.DEP_RFORMPATH.hash, depRformPath.hashCode(),   caseLabel),

                Template.gen(Template.DEP_PATH_PRD.hash, depPath.hashCode(),    prd.regform.hashCode(), caseLabel),
                Template.gen(Template.DEP_VERBPATH_PRD.hash, depVerbPath.hashCode(),    prd.regform.hashCode(), caseLabel),
                Template.gen(Template.DEP_RFORMPATH_PRD.hash, depRformPath.hashCode(),  prd.regform.hashCode(), caseLabel),

                Template.gen(Template.DEP_PATH_ARG.hash, depPath.hashCode(),    arg.regform.hashCode(), caseLabel),
                Template.gen(Template.DEP_VERBPATH_ARG.hash, depVerbPath.hashCode(),    arg.regform.hashCode(), caseLabel),
                Template.gen(Template.DEP_RFORMPATH_ARG.hash, depRformPath.hashCode(),  arg.regform.hashCode(), caseLabel),

                Template.gen(Template.DEP_PATH_PRD_ARG.hash, depPath.hashCode(),    prd.regform.hashCode(), arg.regform.hashCode(), caseLabel),
                Template.gen(Template.DEP_VERBPATH_PRD_ARG.hash, depVerbPath.hashCode(),    prd.regform.hashCode(), arg.regform.hashCode(), caseLabel),
                Template.gen(Template.DEP_RFORMPATH_PRD_ARG.hash, depRformPath.hashCode(),  prd.regform.hashCode(), arg.regform.hashCode(), caseLabel),
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
                int[] tmpFeatIDs = extractLabeledFeatIDs(sample, prd, arg, prdIndex, caseLabel);
                System.arraycopy(tmpFeatIDs, 0, featIDs, (prdIndex*nCases+caseLabel)*N_FEATS, N_FEATS);
            }
        }

        return featIDs;
    }

    private enum Template {
        // Unigrams
        PRD_FORM("p:form"),
        ARG_FORM("a:form"),
        ARG2_FORM("a2:form"),

        PRD_CPOS("p:cpos"),
        ARG_CPOS("a:cpos"),
        ARG2_CPOS("a2:cpos"),

        PRD_POS("p:pos"),
        ARG_POS("a:pos"),
        ARG2_POS("a2:pos"),

        PRD_RFORM("p:rform"),
        ARG_RFORM("a:rform"),
        ARG2_RFORM("a2:rform"),

        PRD_TYPE("p:type"),
        ARG_TYPE("a:type"),

        PRD_INFL("p:infl"),
        ARG_INFL("a:infl"),

        ARG_PARTICLE("a:particle"),
        ARG_COMPCONT("a:compcont"),
        ARG_COMPFUNC("a:compfunc"),

        PRD_SAHEN("p:sahen"),
        PRD_VOICE("p:voice"),

        BI_RFORM_FORM("bi:rform:form"),
        BI_RFORM_CCW("bi:rform:ccw"),
        BI_SAHEN_FORM("bi:sahen:form"),

        BI_RFORM_CFW("bi:rform:cfw"),
        BI_SAHEN_CFW("bi:sahen:cfw"),
        BI_VOICE_CFW("bi:voice:cfw"),
        BI_SAHEN_VOICE_CFW("bi:sahen:voice:cfw"),

        DEP_DIST("dep:dist"),
        DEP_PATH("dep:path"),
        DEP_POSPATH("dep:pos:path"),
        DEP_VERBPATH("dep:verb:path"),
        DEP_RFORMPATH("dep:rform:path"),

        DEP_PATH_PRD("dep:path:prd"),
        DEP_VERBPATH_PRD("dep:verb:path:prd"),
        DEP_RFORMPATH_PRD("dep:rform:path:prd"),

        DEP_PATH_ARG("dep:path:arg"),
        DEP_VERBPATH_ARG("dep:verb:path:arg"),
        DEP_RFORMPATH_ARG("dep:rform:path:arg"),

        DEP_PATH_PRD_ARG("dep:path:prd:arg"),
        DEP_VERBPATH_PRD_ARG("dep:verb:path:prd:arg"),
        DEP_RFORMPATH_PRD_ARG("dep:rform:path:prd:arg"),
        ;

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

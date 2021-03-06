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

    final private int nCases = Config.N_CASES;
    final private static int SIZE = Config.WEIGHT_SIZE;
    final public static int N_FEATS = 42;
    final public static int N_GLOBAL_FEATS = 8;
    
    public FeatureExtractor() {}
    
    final public int[] extractLocalFeatIDs(Sample sample, Chunk prd, Chunk arg, int prdIndex, int caseLabel) {
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
    
    final public int[] extractGlobalFeatIDs(Sample sample, Chunk[] prds, Chunk[] args, int[] caseLabels) {
        Chunk prd1 = prds[0];
        Chunk prd2 = prds[1];
        Chunk arg1 = args[0];
        Chunk arg2 = args[1];
        int caseLabel1 = caseLabels[0];
        int caseLabel2 = caseLabels[1];
        int direct = getDirect(prd1, prd2);
        int posit1 = getPosition(arg1.INDEX, arg2.INDEX, prd1.INDEX, prd2.INDEX, sample.NULL_ARG_INDEX);
        int posit2 = getPosition(arg2.INDEX, arg1.INDEX, prd1.INDEX, prd2.INDEX, sample.NULL_ARG_INDEX);

        int[] featIDs = {
                Template.gen(Template.PAIR_PP.hash,
                             prd1.regform.hashCode(), prd2.regform.hashCode(),
                             prd1.voiceSuffix.hashCode(), prd2.voiceSuffix.hashCode(),
                             caseLabel1, caseLabel2, direct),
                Template.gen(Template.PAIR_AP1.hash,
                             arg1.particle.hashCode(), posit1,
                             prd1.compoundFuncWord.hashCode(), prd1.voiceSuffix.hashCode(),
                             caseLabel1, caseLabel2, direct),
                Template.gen(Template.PAIR_AP2.hash,
                             arg2.particle.hashCode(), posit2,
                             prd2.compoundFuncWord.hashCode(), prd2.voiceSuffix.hashCode(),
                             caseLabel1, caseLabel2, direct),

                Template.gen(Template.TRI_AAP1.hash,
                             arg1.particle.hashCode(), arg2.particle.hashCode(),
                             posit1, posit2,
                             prd1.compoundFuncWord.hashCode(), prd1.voiceSuffix.hashCode(),
                             caseLabel1, caseLabel2, direct),
                Template.gen(Template.TRI_AAP2.hash,
                             arg1.particle.hashCode(), arg2.particle.hashCode(),
                             posit1, posit2,
                             prd2.compoundFuncWord.hashCode(), prd2.voiceSuffix.hashCode(),
                             caseLabel1, caseLabel2, direct),

                Template.gen(Template.QUAD_AX_VO.hash,
                             arg1.particle.hashCode(), arg2.particle.hashCode(),
                             posit1, posit2,
                             prd1.voiceSuffix.hashCode(), prd2.voiceSuffix.hashCode(),
                             caseLabel1, caseLabel2, direct),
                Template.gen(Template.QUAD_AX_AX_VO.hash,
                             arg1.particle.hashCode(), arg2.particle.hashCode(),
                             posit1, posit2,
                             prd1.compoundFuncWord.hashCode(), prd2.compoundFuncWord.hashCode(),
                             prd1.voiceSuffix.hashCode(), prd2.voiceSuffix.hashCode(),
                             caseLabel1, caseLabel2, direct),
                Template.gen(Template.QUAD_AX_REG_VO.hash,
                             arg1.particle.hashCode(), arg2.particle.hashCode(),
                             posit1, posit2,
                             prd1.regform.hashCode(), prd2.regform.hashCode(),
                             prd1.voiceSuffix.hashCode(), prd2.voiceSuffix.hashCode(),
                             caseLabel1, caseLabel2, direct),
        };
        
        return featIDs;
    }
    
    private int getDirect(Chunk prd1, Chunk prd2) {
        if (prd1.INDEX < prd2.INDEX)
            return 1;
        else if (prd1.INDEX > prd2.INDEX)
            return 2;
        return 0;
    }
    
    private int getPosition(int argIndex1, int argIndex2, int prdIndex1, int prdIndex2, int nullIndex) {
        if (argIndex1 == nullIndex)
            return 0;

        if (argIndex1 < argIndex2) {
            if (argIndex1 < prdIndex1) {
                if (argIndex1 < prdIndex2)
                    return 111;
                else if (argIndex1 > prdIndex2)
                    return 112;
                return 113;
            }
            else if (argIndex1 > prdIndex1) {
                if (argIndex1 < prdIndex2)
                    return 121;
                else if (argIndex1 > prdIndex2)
                    return 122;
                return 123;                
            }
            else {
                if (argIndex1 < prdIndex2)
                    return 131;
                else if (argIndex1 > prdIndex2)
                    return 132;
                return 133;                                
            }
        }
        else if (argIndex1 > argIndex2) {
            if (argIndex1 < prdIndex1) {
                if (argIndex1 < prdIndex2)
                    return 211;
                else if (argIndex1 > prdIndex2)
                    return 212;
                return 213;
            }
            else if (argIndex1 > prdIndex1) {
                if (argIndex1 < prdIndex2)
                    return 221;
                else if (argIndex1 > prdIndex2)
                    return 222;
                return 223;                
            }
            else {
                if (argIndex1 < prdIndex2)
                    return 231;
                else if (argIndex1 > prdIndex2)
                    return 232;
                return 233;                                
            }
        }
        else {
            if (argIndex1 < prdIndex1) {
                if (argIndex1 < prdIndex2)
                    return 311;
                else if (argIndex1 > prdIndex2)
                    return 312;
                return 313;
            }
            else if (argIndex1 > prdIndex1) {
                if (argIndex1 < prdIndex2)
                    return 321;
                else if (argIndex1 > prdIndex2)
                    return 322;
                return 323;                
            }
            else {
                if (argIndex1 < prdIndex2)
                    return 331;
                else if (argIndex1 > prdIndex2)
                    return 332;
                return 333;                                
            }
        }
    }
    
    final public int[] getOracleLocalFeatIDs(Sample sample) {
        int[] featIDs = new int[sample.prds.length * nCases * N_FEATS];

        for (int prdIndex=0; prdIndex<sample.prds.length; ++prdIndex) {
            Chunk prd = sample.prds[prdIndex];
            int[] argIndices = sample.oracleGraph[prdIndex];
            
            for (int caseLabel=0; caseLabel<argIndices.length; ++caseLabel) {
                Chunk arg = sample.args[argIndices[caseLabel]];
                int[] tmpFeatIDs = extractLocalFeatIDs(sample, prd, arg, prdIndex, caseLabel);
                System.arraycopy(tmpFeatIDs, 0, featIDs, (prdIndex*nCases+caseLabel)*N_FEATS, N_FEATS);
            }
        }

        return featIDs;
    }
    
    final public int[] getOracleGlobalFeatIDs(Sample sample) {
        int[][] graph = sample.oracleGraph;
        int nPrds = graph.length;
        int[] featIDs = new int[combination(nPrds * nCases) * N_GLOBAL_FEATS];
        int count = 0;

        for (int prdIndex1=0; prdIndex1<nPrds; ++prdIndex1) {
            Chunk prd1 = sample.prds[prdIndex1];
            int[] tmpGraph1 = graph[prdIndex1];

            for (int caseLabel1=0; caseLabel1<nCases; ++caseLabel1) {
                int argIndex1 = tmpGraph1[caseLabel1];
                Chunk arg1 = sample.args[argIndex1];

                for (int prdIndex2=prdIndex1; prdIndex2<nPrds; ++prdIndex2) {
                    Chunk prd2 = sample.prds[prdIndex2];
                    Chunk[] prds = getReorderPrds(prd1, prd2);
                    
                    int[] tmpGraph2 = graph[prdIndex2];
                    
                    int initCaseLabel = 0;
                    if (prdIndex1 == prdIndex2)
                        initCaseLabel = caseLabel1+1;

                    for (int caseLabel2=initCaseLabel; caseLabel2<nCases; ++caseLabel2) {
                        int argIndex2 = tmpGraph2[caseLabel2];
                        Chunk arg2 = sample.args[argIndex2];
                        Chunk[] args = getReorderArgs(arg1, arg2);

                        int[] tmpFeatIDs = extractGlobalFeatIDs(sample, prds, args, new int[]{caseLabel1, caseLabel2});
                        System.arraycopy(tmpFeatIDs, 0, featIDs, count*N_GLOBAL_FEATS, N_GLOBAL_FEATS);
                        count++;
                    }
                }
            }
        }
        return featIDs;        
    }

    final public int[] getLocalFeatIDs(int[][] graph, ScoreTable scoreTable) {
        int[] featIDs = new int[graph.length * nCases * N_FEATS];

        for (int prdIndex=0; prdIndex<graph.length; ++prdIndex) {
            int[] argIndices = graph[prdIndex];
            
            for (int caseLabel=0; caseLabel<argIndices.length; ++caseLabel) {
                int argIndex = argIndices[caseLabel];
                int[] tmpFeatIDs = scoreTable.localFeatIDs[prdIndex][argIndex][caseLabel];
                System.arraycopy(tmpFeatIDs, 0, featIDs, (prdIndex*nCases+caseLabel)*N_FEATS, N_FEATS);
            }
        }

        return featIDs;
    }

    final public int[] getGlobalFeatIDs(int[][] graph, ScoreTable scoreTable) {
        int nPrds = graph.length;
        int[] featIDs = new int[combination(nPrds * nCases) * N_GLOBAL_FEATS];
        int count = 0;

        for (int prdIndex1=0; prdIndex1<nPrds; ++prdIndex1) {
            int[] tmpGraph1 = graph[prdIndex1];
            int[][][][][][] feats1 = scoreTable.globalFeatIDs[prdIndex1];

            for (int caseLabel1=0; caseLabel1<nCases; ++caseLabel1) {
                int argIndex1 = tmpGraph1[caseLabel1];
                int[][][][] feats2 = feats1[argIndex1][caseLabel1];

                for (int prdIndex2=prdIndex1; prdIndex2<nPrds; ++prdIndex2) {
                    int[] tmpGraph2 = graph[prdIndex2];
                    int[][][] feats3 = feats2[prdIndex2];
                    
                    int initCaseLabel = 0;
                    if (prdIndex1 == prdIndex2)
                        initCaseLabel = caseLabel1+1;

                    for (int caseLabel2=initCaseLabel; caseLabel2<nCases; ++caseLabel2) {
                        int argIndex2 = tmpGraph2[caseLabel2];
                        int[] tmpFeatIDs = feats3[argIndex2][caseLabel2];
                        System.arraycopy(tmpFeatIDs, 0, featIDs, count*N_GLOBAL_FEATS, N_GLOBAL_FEATS);
                        count++;
                    }
                }
            }
        }
        return featIDs;
    }

    private int combination(int n) {
        int sum = 1;
        for (int i=n; i>n-2; --i)
            sum *= i;
        return sum / 2;
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

        PAIR_PP("pair:pp"),
        PAIR_AP1("pair:ap1"),
        PAIR_AP2("pair:ap2"),

        TRI_AAP1("tri:aap1"),
        TRI_AAP2("tri:aap2"),

        QUAD_AX_VO("tri:ax:vo"),
        QUAD_AX_AX_VO("tri:ax:ax:vo"),
        QUAD_AX_REG_VO("tri:ax:reg:vo"),
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

    private Chunk[] getReorderPrds(Chunk prd1, Chunk prd2) {
        if (prd1.prd.FORM.hashCode() < prd2.prd.FORM.hashCode())
            return new Chunk[]{prd1, prd2};
//        return new Chunk[]{prd2, prd1};
        return new Chunk[]{prd1, prd2};
    }

    private Chunk[] getReorderArgs(Chunk arg1, Chunk arg2) {
        if (arg1.chead.FORM.hashCode() < arg2.chead.FORM.hashCode())
            return new Chunk[]{arg1, arg2};
//        return new Chunk[]{arg2, arg1};
        return new Chunk[]{arg1, arg2};
    }
    
}

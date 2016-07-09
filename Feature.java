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

public class Feature implements Serializable{
    //[case][case][prd][prd][arg][arg]
    public ArrayList[][][][][][] cache;
    public ArrayList<Sentence> sents;
    public int nCases;
    public int weightSize;
    
    public Feature(int nCases) {
        this.nCases = nCases;
    }
    
    final public ArrayList getFeature(Sentence sent, int argIndex, int prdIndex, int caseLabel) {
        ArrayList<Integer> usedFeatures = new ArrayList();
        Chunk[] phiWindow = getPhiWindow(argIndex, prdIndex, sent);

        // Unigram feature
        usedFeatures.addAll(getUniFeature(phiWindow, caseLabel));
        
        // Combinatorial features
        usedFeatures.addAll(getBiFeature(phiWindow, sent, caseLabel));
            
        return usedFeatures;
    }

    final public ArrayList getWordFeature(Sentence sent, int argIndex, int prdIndex, int caseLabel) {
        ArrayList<Integer> usedFeatures = new ArrayList();
        Word[] phiWindow = getPhiWindow(argIndex, prdIndex, sent.words);

        // Unigram feature
        usedFeatures.addAll(getUniFeature(phiWindow, caseLabel));
        
        // Combinatorial features
        ArrayList biPhi = getBiFeature(phiWindow, sent, caseLabel);
        if (biPhi != null) usedFeatures.addAll(biPhi);
            
        return usedFeatures;
    }


    final public ArrayList getFeature(Sentence sent, int[][] graph){
        ArrayList<Integer> features = new ArrayList();

        final int[][] transposedGraph = getTransposedGraph(graph);
        
        for (int caseLabel_i=0; caseLabel_i<nCases; caseLabel_i++) {
            final int[] caseGraph_i = transposedGraph[caseLabel_i];
            final ArrayList[][][][][] caseCache_i = cache[caseLabel_i];
            
            for (int caseLabel_j=caseLabel_i; caseLabel_j<nCases; caseLabel_j++) {
                final int[] caseGraph_j = transposedGraph[caseLabel_j];
                final ArrayList[][][][] caseCache_ij = caseCache_i[caseLabel_j];                
                features.addAll(getFeature(sent, caseGraph_i, caseGraph_j,
                                           caseLabel_i, caseLabel_j, caseCache_ij));
            }
        }

        return features;
    }
    
    private int[][] getTransposedGraph(int[][] graph) {
        int[][] trasposedGraph = new int[nCases][graph.length];
        for (int prd_i=0; prd_i<graph.length; prd_i++)
            for (int case_label=0; case_label<nCases; case_label++)
                trasposedGraph[case_label][prd_i] = graph[prd_i][case_label];
        return trasposedGraph;
    }
        
    private ArrayList<Integer> getFeature(Sentence sent, int[] graph1, int[] graph2,
                                           int caseLabel1, int caseLabel2,
                                           ArrayList[][][][] caseCache) {    
        final String[] feature = new String[12];
        final ArrayList<Integer> phiIds = new ArrayList();

        final ArrayList<Chunk> chunks = sent.chunks;
        final ArrayList<Integer> argIndices = sent.argIndices;
        final ArrayList<Integer> prdIndices = sent.prdIndices;
        final int nArgs = argIndices.size();
        final int nPrds = prdIndices.size();

        final String caseLabel = caseLabel1 + "_" + caseLabel2;
        
        for (int prd_i=0; prd_i<nPrds; ++prd_i) {
            int arg_i = graph1[prd_i];
            int prdIndex_i = prdIndices.get(prd_i);
            int argIndex_i = argIndices.get(arg_i);

            if (prdIndex_i == argIndex_i) continue;                

            ArrayList[][] tmpCache_i = caseCache[prd_i][arg_i];
            
            int nextPrd_i;
            if (caseLabel1 == caseLabel2) nextPrd_i = prd_i + 1;
            else nextPrd_i = prd_i;
            
            for (int prd_j=nextPrd_i; prd_j<nPrds; ++prd_j) {
                int phiIndex = 0;
                
                int arg_j = graph2[prd_j];                        
                int prdIndex_j = prdIndices.get(prd_j);
                int argIndex_j = argIndices.get(arg_j);
                    
                if (prdIndex_j == argIndex_j) continue;                
                
                ArrayList tmpCache_j = tmpCache_i[prd_j][arg_j];
                if (tmpCache_j != null) {
                    phiIds.addAll(tmpCache_j);
                    continue;
                }
                
                Chunk arg1 = chunks.get(argIndex_i);
                Chunk prd1 = chunks.get(prdIndex_i);                    
                String aux1 = arg1.aux;
                String prdRform1 = prd1.reg_form;
                String argPath1 = sent.depPath[arg_i][prdIndex_i];
                String compJoshi1 = prd1.compoundJoshi;                
                String alt1 = prd1.caseAlter;            
                
                Chunk arg2 = chunks.get(argIndex_j);                
                Chunk prd2 = chunks.get(prdIndex_j);
                String prdRform2 = prd2.reg_form;                    
                String compJoshi2 = prd2.compoundJoshi;                
                String alt2 = prd2.caseAlter;
                String direct = direction(prdIndex_i, prdIndex_j);
                String conj = caseLabel + direct;
                
                //Feature
                if (argIndex_i != argIndex_j) {
                    // Diff-Arg
                    String[] posit = Feature.this.position(argIndex_i, argIndex_j, prdIndex_i, prdIndex_j, nArgs);
                    String argPosit1 = posit[0];
                    String argPosit2 = posit[1];
                    String combPosit = argPosit1 + argPosit2;
                    String aux2 = arg2.aux;
                    String joshi = aux1 + aux2 + alt1 + alt2 + argPosit1 + argPosit2;
                    
                    // pair
                    feature[phiIndex++] = "101" + joshi + conj;
                    feature[phiIndex++] = "102" + prdRform1 + prdRform2 + alt1 + alt2 + conj;

                    // first-order
                    feature[phiIndex++] = "111" + aux1 + compJoshi1 + alt1 + argPosit1 + conj;
                    feature[phiIndex++] = "112" + aux2 + compJoshi2 + alt2 + argPosit2 + conj;

                    // triangle
                    feature[phiIndex++] = "121" + aux1 + aux2 + compJoshi1 + alt1 + combPosit + conj;
                    feature[phiIndex++] = "122" + aux1 + aux2 + compJoshi2 + alt2 + combPosit + conj;
                    
                    // quadrilateral add posit
                    feature[phiIndex++] = "131" + joshi + compJoshi1 + compJoshi2 + combPosit + conj;
                    feature[phiIndex++] = "132" + prdRform1 + prdRform2 + joshi + combPosit + conj;
                }
                else {
                    // Co-Arg
                    String posit = position(argIndex_i, prdIndex_i, prdIndex_j, nArgs);                                        
                    String prdPath = sent.depPath[prdIndex_i][prdIndex_j];
                    String argPath2 = sent.depPath[arg_i][prdIndex_j];                                                            
                    String prdJoshi = compJoshi1 + alt1 + compJoshi2 + alt2 + aux1 + conj + posit;

                    if ("0".equals(prdPath))
                        feature[phiIndex++] = "201" + prdJoshi;
                
                    if ("0".equals(argPath1))
                        feature[phiIndex++] = "203" + prdJoshi;
                
                    if ("0".equals(argPath2))
                        feature[phiIndex++] = "205" + prdJoshi;
                
                    if ("1".equals(argPath1))
                        feature[phiIndex++] = "207" + prdJoshi;
                
                    if ("1".equals(argPath2))
                        feature[phiIndex++] = "209" + prdJoshi;
                    
                    feature[phiIndex++] = "211" + prdRform1 + prdRform2 + conj + posit;
                    feature[phiIndex++] = "212" + prdRform1 + prdRform2 + conj + aux1 + posit;
                }
                
                // Feature ID registration
                ArrayList phiId = new ArrayList();                
                for(int l=0; l<phiIndex; ++l){
                    if (feature[l] == null) continue;
                    String f = feature[l];
                    phiId.add((f.hashCode() >>> 1) % weightSize);
                }
                
                phiIds.addAll(phiId);

                if (tmpCache_j == null)
                    cache[caseLabel1][caseLabel2][prd_i][arg_i][prd_j][arg_j] = phiId;
            }
        }
                            
        return phiIds;
    }
                
    private String direction(final int prdIndex1, final int prdIndex2) {
        if (prdIndex1 < prdIndex2) return "L";
        else if (prdIndex1 > prdIndex2) return "R";
        else return "N";
    }

    private String[] position(int argIndex1, int argIndex2, int prdIndex1, int prdIndex2, int nArgs) {
        String posit1 = ""; 
        String posit2 = "";

        if (argIndex1 < argIndex2) {
            posit1 += "0";
            posit2 += "1";
        }
        else {
            posit1 += "1";
            posit2 += "0";
        }            
    
        if (argIndex1 < prdIndex1) posit1 += "0";
        else if (argIndex1 > prdIndex1) posit1 += "1";
        else posit1 += "2";

        if (argIndex2 < prdIndex1) posit2 += "0";
        else if (argIndex2 > prdIndex1) posit2 += "1";
        else posit2 += "2";

        if (argIndex1 < prdIndex2) posit1 += "0";
        else if (argIndex1 > prdIndex2) posit1 += "1";
        else posit1 += "2";

        if (argIndex2 < prdIndex2) posit2 += "0";
        else if (argIndex2 > prdIndex2) posit2 += "1";
        else posit2 += "2";
        
        if (argIndex1 == nArgs-1)
            posit1 = "N";            
        
        if (argIndex2 == nArgs-1)
            posit2 = "N";            
        
        return new String[]{posit1, posit2};
    }

    
    private String position(int argIndex1, int prdIndex1, int prdIndex2, int nArgs) {
        String posit = "";
        if (argIndex1 == nArgs-1)
            return "N";
        
        if (argIndex1 < prdIndex1) posit += "0";
        else if (argIndex1 > prdIndex1) posit += "1";
        else posit += "2";

        if (argIndex1 < prdIndex2) posit += "0";
        else if (argIndex1 > prdIndex2) posit += "1";
        else posit += "2";
        
        return posit;
    }

    private ArrayList getUniFeature(Chunk[] feats, int caseLabel) {
        String[] feature = new String[30];
        int k = 0;
        
        for(int i=0; i<3; ++i){
            if (feats[i] == null) continue;

            final Chunk f = feats[i];
            final Word chead = f.chead;
            
            if (chead != null) {
                feature[k++] = "1" + i + chead.R_FORM + caseLabel;                
                feature[k++] = "2" + i + chead.CPOS + caseLabel;
                feature[k++] = "3" + i + chead.POS + caseLabel;
                
                if (i > 1) continue;
                
                feature[k++] = "4" + i + chead.FORM + caseLabel;

                if (!"*".equals(chead.INF_TYPE))
                    feature[k++] = "5" + i + chead.INF_TYPE + caseLabel;
                if (!"*".equals(chead.INF_FORM))
                    feature[k++] = "6" + i + chead.INF_FORM + caseLabel;
                
                if (i==0) {
                    feature[k++] = "7" + f.aux + caseLabel;
                    
                    if (!"".equals(f.compoundNoun))
                        feature[k++] = "8" + f.compoundNoun + caseLabel;

                    if (!"".equals(f.compoundJoshi))
                        feature[k++] = "9" + f.compoundJoshi + caseLabel;
                }

                if (i==1) {
                    feature[k++] = "10" + f.caseAlter + caseLabel;

                    if (!"".equals(f.compoundSahenNoun)) {
                        feature[k++] = "11" + f.compoundSahenNoun + caseLabel;
                    }
                }               
            }
        }

        return getPhiId(feature);        
    }
    
    
    private ArrayList getUniFeature(Word[] phiWindow, int caseLabel) {
        String[] feature = new String[20];
        int phiIndex = 0;
        
        for(int i=0; i<3; ++i){
            if (phiWindow[i] == null) continue;

            Word word = phiWindow[i];
            
            if (word != null) {
                feature[phiIndex++] = "1" + i + word.R_FORM + caseLabel;                
                feature[phiIndex++] = "2" + i + word.CPOS + caseLabel;
                feature[phiIndex++] = "3" + i + word.POS + caseLabel;
                
                if (i > 1) continue;
                
                feature[phiIndex++] = "4" + i + word.FORM + caseLabel;

                if (!"*".equals(word.INF_TYPE))
                    feature[phiIndex++] = "5" + i + word.INF_TYPE + caseLabel;
                if (!"*".equals(word.INF_FORM))
                    feature[phiIndex++] = "6" + i + word.INF_FORM + caseLabel;
                
            }
        }

        return getPhiId(feature);
    }
    
    private ArrayList getBiFeature(Chunk[] feats, Sentence sent, int caseLabel) {
        String[] feature = new String[30];
        int phiIndex = 0;

        Chunk phi1 = feats[0];
        Chunk phi2 = feats[1];
        
        if (phi1 != null && phi2 != null) {
            Word chead1 = phi1.chead;
            Word chead2 = phi2.chead;
            
            if (chead1 != null && chead2 != null) {
                feature[phiIndex++] = "31" + chead1.FORM + chead2.R_FORM + caseLabel;

                if (!"".equals(phi1.compoundNoun))
                    feature[phiIndex++] = "32" + phi1.compoundNoun + chead2.R_FORM + caseLabel;

                if (!"".equals(phi2.compoundSahenNoun))
                    feature[phiIndex++] = "33" + chead1.FORM + phi2.compoundSahenNoun + caseLabel;
                
                if (!"".equals(phi1.compoundJoshi)) {
                    feature[phiIndex++] = "34" + phi1.compoundJoshi + chead2.R_FORM + caseLabel;
                    feature[phiIndex++] = "35" + phi1.compoundJoshi + phi2.compoundSahenNoun + caseLabel;

                    feature[phiIndex++] = "36" + phi1.compoundJoshi + phi2.caseAlter + caseLabel;
                    feature[phiIndex++] = "37" + phi1.compoundJoshi + phi2.compoundSahenNoun + phi2.caseAlter + caseLabel;
                }
            }
            
            // Distance
            int dist = phi2.INDEX - phi1.INDEX;
            String pre_post;
            
            if (dist > 0) pre_post = "PRE";
            else pre_post = "POST";

            feature[phiIndex++] = "41_" + pre_post + "_" + caseLabel;
            feature[phiIndex++] = "42_" + pre_post + "_" + dist + "_" + caseLabel;
            
            // Dependency Path
            String label = phi2.caseAlter + caseLabel;

            int argIndex;
            if (phi1.INDEX > -1) argIndex = phi1.INDEX;
            else argIndex = sent.size()-1;
            int prdIndex = phi2.INDEX;

            int depDist = sent.depDist[argIndex][prdIndex];
            String dpath = sent.depPath[argIndex][prdIndex] + label;
            String dpath0 = sent.depPosPath[argIndex][prdIndex] + label;
            String dpath1 = sent.depVerbPath[argIndex][prdIndex] + label;
            String dpath2 = sent.depRformPath[argIndex][prdIndex] + label;            

            
            feature[phiIndex++] = "43" + depDist + caseLabel;
            
            feature[phiIndex++] = "44" + dpath;
            feature[phiIndex++] = "45" + dpath0;
            feature[phiIndex++] = "46" + dpath1;
            feature[phiIndex++] = "47" + dpath2;

            String p_form = phi2.reg_form;
            String a_form = phi1.reg_form;
            
            feature[phiIndex++] = "51" + dpath + p_form;
            feature[phiIndex++] = "52" + dpath1 + p_form;
            feature[phiIndex++] = "53" + dpath2 + p_form;

            feature[phiIndex++] = "54" + dpath + a_form;
            feature[phiIndex++] = "55" + dpath1 + a_form;
            feature[phiIndex++] = "56" + dpath2 + a_form;
            
            feature[phiIndex++] = "57" + dpath + a_form + p_form;
            feature[phiIndex++] = "58" + dpath1 + a_form + p_form;
            feature[phiIndex++] = "59" + dpath2 + a_form + p_form;
            
        }
        
        return getPhiId(feature);
    }
    
    private ArrayList getBiFeature(Word[] phiWindow, Sentence sent, int caseLabel) {
        String[] feature = new String[30];
        int phiIndex = 0;

        Word word1 = phiWindow[0];
        Word word2 = phiWindow[1];
        
        if (word1 != null && word2 != null) {
            feature[phiIndex++] = "31" + word1.FORM + "_" + word2.FORM + "_" + caseLabel;
            
            // Distance
            int dist = word2.INDEX - word1.INDEX;
            String pre_post;
            
            if (dist > 0) pre_post = "PRE";
            else pre_post = "POST";

            feature[phiIndex++] = "41_" + pre_post + "_" + caseLabel;
            feature[phiIndex++] = "42_" + pre_post + "_" + dist + "_" + caseLabel;
            
            // Dependency Path
            int argIndex;
            if (word1.INDEX > -1) argIndex = word1.INDEX;
            else argIndex = sent.size()-1;
            int prdIndex = word2.INDEX;
            
            if (argIndex < 0 || prdIndex < 0)
                return null;

            int depDist = sent.depDist[argIndex][prdIndex];
            String dpath = sent.depPath[argIndex][prdIndex] + caseLabel;
            String dpath0 = sent.depPosPath[argIndex][prdIndex] + caseLabel;

            
            feature[phiIndex++] = "43" + depDist + caseLabel;
            
            feature[phiIndex++] = "44" + dpath;
            feature[phiIndex++] = "45" + dpath0;

            String a_form = word1.R_FORM;
            String p_form = word2.R_FORM;
            
            feature[phiIndex++] = "51" + dpath + p_form;
            feature[phiIndex++] = "54" + dpath + a_form;            
            feature[phiIndex++] = "57" + dpath + a_form + p_form;
            
        }
        
        return getPhiId(feature);
    }

    private ArrayList getPhiId(String[] feature) {
        ArrayList<Integer> featureId = new ArrayList();
        for(int i=0; i<feature.length; ++i) {
            if (feature[i] == null) break;
            featureId.add((feature[i].hashCode() >>> 1) % weightSize);
        }
        return featureId;
    }
    
    private Chunk[] getPhiWindow(int argIndex, int prdIndex, Sentence sent) {
        Chunk[] phiWindow = new Chunk[3];
        phiWindow[0] = sent.chunks.get(argIndex);
        phiWindow[1] = sent.chunks.get(prdIndex);

        int n1 = phiWindow[0].INDEX + 1;
        if (n1 > -1 && n1 < sent.size() && n1 != phiWindow[1].INDEX)
            phiWindow[2] = sent.chunks.get(n1);

        return phiWindow;
    }
    
    private Word[] getPhiWindow(int arg_i, int prdIndex, ArrayList<Word> words) {
        Word[] phiWindow = new Word[3];
        phiWindow[0] = words.get(arg_i);
        phiWindow[1] = words.get(prdIndex);

        int nextArgIndex = arg_i + 1;
        if (nextArgIndex > -1 && nextArgIndex < words.size())
            phiWindow[2] = words.get(nextArgIndex);

        return phiWindow;
    }
    
}

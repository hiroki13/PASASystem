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
    
    final public ArrayList getFeature(Sentence sent, int argIndex, int prdIndex, int caseLabel){
        ArrayList<Integer> usedFeatures = new ArrayList();
        Chunk[] phiWindow = getPhiWindow(argIndex, prdIndex, sent);

        // Unigram feature
        usedFeatures.addAll(getUniFeature(phiWindow, caseLabel));
        
        // Combinatorial features
        usedFeatures.addAll(getBiFeature(phiWindow, sent, caseLabel));
            
        return usedFeatures;
    }

    final public ArrayList getWordFeature(Sentence sent, int argIndex, int prdIndex, int caseLabel){
        ArrayList<Integer> usedFeatures = new ArrayList();
        Word[] phiWindow = getPhiWindow(argIndex, prdIndex, sent.words);

        // Unigram feature
        usedFeatures.addAll(getUniFeature(phiWindow, caseLabel));
        
        // Combinatorial features
        ArrayList biPhi = getBiFeature(phiWindow, sent, caseLabel);
        if (biPhi != null)
            usedFeatures.addAll(biPhi);
            
        return usedFeatures;
    }


    final public ArrayList getFeature(Sentence sent, int[][] graph){
        ArrayList<Integer> usedFeatures = new ArrayList<>();

        ArrayList<Chunk> chunks = sent.chunks;
        ArrayList<Integer> args = sent.argIndices;
        ArrayList<Integer> prds = sent.prdIndices;
        int nPrds = prds.size();
        
        final int[][] t_graph = genTGraph(nPrds, graph); // tenchi
        
        for (int case_label1=0; case_label1<nCases; case_label1++) {
            final int[] tmp_graph1 = t_graph[case_label1];
            final ArrayList[][][][][] tmp_cache1 = cache[case_label1];
            
            for (int case_label2=case_label1; case_label2<nCases; case_label2++) {
                final int[] tmp_graph2 = t_graph[case_label2];
                final ArrayList[][][][] tmp_cache2 = tmp_cache1[case_label2];
                
                usedFeatures.addAll(extractFeature(sent, tmp_graph1,
                                                   tmp_graph2, case_label1,
                                                   case_label2, chunks,
                                                   args, prds, nPrds,
                                                   tmp_cache2));
            }
        }

        return usedFeatures;
    }
    
    private int[][] genTGraph(final int prd_length, final int[][] graph) {
        final int[][] t_graph = new int[nCases][prd_length];
        for (int prd_i=0; prd_i<prd_length; prd_i++) {
            for (int case_label=0; case_label<nCases; case_label++) {
                t_graph[case_label][prd_i] = graph[prd_i][case_label];
            }
        }
        return t_graph;
    }
        
    private ArrayList<Integer> extractFeature(final Sentence sentence,
                                                      final int[] graph1,
                                                      final int[] graph2,
                                                      final int case_label1,
                                                      final int case_label2,
                                                      final ArrayList<Chunk> chunks,
                                                      final ArrayList<Integer> args,
                                                      final ArrayList<Integer> prds,
                                                      final int prd_length,
                                                      ArrayList[][][][] tmp_cache) {    
        final String[] feature = new String[12];
        final ArrayList<Integer> usedFeatures = new ArrayList<>();
        final int arg_length = args.size();

        ArrayList tmp_feature;
        final String c_label = case_label1 + "_" + case_label2;


        Chunk arg1 = null;                        
        Chunk prd1 = null;                                    
        String aux1 = "";                        
        String prd1_rform = "";
        String arg_path1 = null;
        String comp_joshi1 = "";
        String alt1 = "";
        
        for (int prd_i=0; prd_i<prd_length; ++prd_i) {
            final int prd_id1 = prds.get(prd_i);
            final int a1 = graph1[prd_i];
            final int arg_id1 = args.get(a1);

            if (prd_id1 == arg_id1)
                continue;                

            final ArrayList[][] tmp_cache1 = tmp_cache[prd_i][a1];
            
            
            final int p;
            if (case_label1 == case_label2) p = prd_i + 1;
            else p = prd_i;
            
            for (int prd_j=p; prd_j<prd_length; ++prd_j) {
                int k = 0;
                
                final int a2 = graph2[prd_j];                        
                final int prd_id2 = prds.get(prd_j);
                final int arg_id2 = args.get(a2);
                    
                if (prd_id2 == arg_id2)
                    continue;                
                
                // cache
                final ArrayList tmp_cache2 = tmp_cache1[prd_j][a2];
                if (tmp_cache2 != null) {
                    usedFeatures.addAll(tmp_cache2);
                    continue;
                }
                
                if (k == 0) {
                    arg1 = chunks.get(arg_id1);
                    prd1 = chunks.get(prd_id1);                    
                    aux1 = arg1.aux;
                    prd1_rform = prd1.reg_form;
                    arg_path1 = sentence.depPath[a1][prd_id1];
                    comp_joshi1 = prd1.compound_joshi;
                    alt1 = prd1.case_alter;
                }                
            
                
                final Chunk arg2 = chunks.get(arg_id2);                
                final Chunk prd2 = chunks.get(prd_id2);
                final String prd2_rform = prd2.reg_form;                    
                final String comp_joshi2 = prd2.compound_joshi;                
                final String alt2 = prd2.case_alter;
                final String direct = direction(prd_id1, prd_id2);
                final String conj = c_label + direct;
                
                //Feature
                if (arg_id1 != arg_id2) {
                    // Diff-Arg

                    final String[] tmp_posit = position(arg_id1, arg_id2, prd_id1,
                                                        prd_id2, arg_length);
                    final String a_posit1 = tmp_posit[0];
                    final String a_posit2 = tmp_posit[1];
                    final String posit = a_posit1 + a_posit2;
                    final String aux2 = arg2.aux;
                    final String joshi = aux1 + aux2 + alt1 + alt2
                                         + a_posit1 + a_posit2;
                    
                    // pair
                    feature[k++] = "101" + joshi + conj;
                    feature[k++] = "102" + prd1_rform + prd2_rform + alt1 + alt2 + conj;

                    // first-order
                    feature[k++] = "111" + aux1 + comp_joshi1
                                    + alt1 + a_posit1 + conj;
                    feature[k++] = "112" + aux2 + comp_joshi2
                                    + alt2 + a_posit2 + conj;

                    // triangle
                    feature[k++] = "121" + aux1 + aux2 + comp_joshi1
                                    + alt1 + posit + conj;
                    feature[k++] = "122" + aux1 + aux2 + comp_joshi2
                                    + alt2 + posit + conj;
                    
                    
                    // quadrilateral add posit
                    feature[k++] = "131" + joshi + comp_joshi1
                                    + comp_joshi2 + posit + conj;
                    feature[k++] = "132" + prd1_rform + prd2_rform
                                    + joshi + posit + conj;
                }
                else {
                    // Co-Arg

                    final String a_posit = position_coarg(arg_id1, prd_id1,
                                                          prd_id2, arg_length);                                        
                    final String prd_path = sentence.depPath[prd_id1][prd_id2];
                    final String arg_path2 = sentence.depPath[a1][prd_id2];
                                                            
                    final String prd_joshi = comp_joshi1 + alt1
                                             + comp_joshi2 + alt2
                                             + aux1 + conj + a_posit;


                    if ("0".equals(prd_path)) {
                        feature[k++] = "201" + prd_joshi;
                    }
                
                    if ("0".equals(arg_path1)) {
                        feature[k++] = "203" + prd_joshi;
                    }
                
                    if ("0".equals(arg_path2)) {
                        feature[k++] = "205" + prd_joshi;
                    }
                
                    if ("1".equals(arg_path1)) {
                        feature[k++] = "207" + prd_joshi;
                    }
                
                    if ("1".equals(arg_path2)) {
                        feature[k++] = "209" + prd_joshi;
                    }
                    
                    feature[k++] = "211" + prd1_rform + prd2_rform
                                    + conj + a_posit;
                    feature[k++] = "212" + prd1_rform + prd2_rform
                                    + conj + aux1 + a_posit;
                                        
                }
                
                // Feature ID registration
                tmp_feature = new ArrayList<>();
                
                for(int l=0; l<k; ++l){
                    if (feature[l] == null) continue;
                    String f = feature[l];
                    tmp_feature.add((f.hashCode() >>> 1) % weightSize);
                }
                
                usedFeatures.addAll(tmp_feature);

                if (tmp_cache2 == null) {
                    cache[case_label1][case_label2]
                         [prd_i][a1][prd_j][a2] = tmp_feature;
                }
            }
        }
                            
        return usedFeatures;
    }

    
    final public ArrayList<Integer> extractFeature(final Sentence sentence,
                                                     final int[] graph,
                                                     final int prd_i) {
        final String[] feature = new String[1];
        final ArrayList<Integer> usedFeatures = new ArrayList<>();
        final int arg_length = sentence.argIndices.size();
        int k=0;
        final ArrayList<Chunk> chunks = sentence.chunks;
        final ArrayList<Integer> prd_ids = sentence.prdIndices;
        final ArrayList<Integer> arg_ids = sentence.argIndices;
        final int prd_id = prd_ids.get(prd_i);
        final Chunk prd = chunks.get(prd_id);
        String dep;

        String frame = "301" + prd.reg_form + prd.case_alter;
//        String frame2 = "302" + prd.reg_form + prd.case_alter;
//        String frame3 = "303" + prd.reg_form + prd.case_alter;
//        String frame4 = "304" + prd.reg_form + prd.case_alter;
//        String frame5 = "305" + prd.reg_form + prd.case_alter;
//        String frame6 = "306" + prd.compound_joshi + prd.case_alter;

        for (int i=0; i<graph.length; ++i) {
            final int arg_id = arg_ids.get(graph[i]);            
            final Chunk arg = chunks.get(arg_id);
            
            if (arg_id != arg_length-1) {
                frame += "1";
//                frame2 += arg.reg_form;
//                frame3 += position(arg.index, prd.index);
//                frame4 += arg.aux;
//                frame5 += arg.compound_joshi;
//                frame6 +=  depInfo(sentence.dep_path[arg_id][prd_id]);
            }
            else {
                frame += "0";
//                frame2 += "0";
//                frame3 += "D";
//                frame4 += "NONE";
//                frame5 += "NONE";
//                frame6 += "N";
            }
        }
        feature[k++] = frame;
//        feature[k++] = frame2;
//        feature[k++] = frame3;
//        feature[k++] = frame4;
//        feature[k++] = frame5;
//        feature[k++] = frame6;
        

        for(int i=0; i<feature.length; ++i) {
            String f = feature[i];
            usedFeatures.add((f.hashCode() >>> 1) % weightSize);
        }
        
        return usedFeatures;
    }
                
    private String direction(final int prd_id1, final int prd_id2) {
        if (prd_id1 < prd_id2) return "L";
        else if (prd_id1 > prd_id2) return "R";
        else return "N";
    }

    private String[] position(final int arg_id1, final int arg_id2,
                                     final int prd_id1, final int prd_id2,
                                     final int arg_length) {
        String a_posit1 = ""; 
        String a_posit2 = "";

        if (arg_id1 < arg_id2) {
            a_posit1 += "0";
            a_posit2 += "1";
        }
        else {
            a_posit1 += "1";
            a_posit2 += "0";
        }            
    
        if (arg_id1 < prd_id1) a_posit1 += "0";
        else if (arg_id1 > prd_id1) a_posit1 += "1";
        else a_posit1 += "2";

        if (arg_id2 < prd_id1) a_posit2 += "0";
        else if (arg_id2 > prd_id1) a_posit2 += "1";
        else a_posit2 += "2";

        if (arg_id1 < prd_id2) a_posit1 += "0";
        else if (arg_id1 > prd_id2) a_posit1 += "1";
        else a_posit1 += "2";

        if (arg_id2 < prd_id2) a_posit2 += "0";
        else if (arg_id2 > prd_id2) a_posit2 += "1";
        else a_posit2 += "2";
        
        if (arg_id1 == arg_length-1) {        
            a_posit1 = "N";            
        }
        
        if (arg_id2 == arg_length-1) {        
            a_posit2 = "N";            
        }                    
        
        return new String[]{a_posit1, a_posit2};
    }

    
    private String position_coarg(final int arg_id1, final int prd_id1,
                                          final int prd_id2, final int arg_length) {
        String posit = "";
        if (arg_id1 == arg_length-1)
            return "N";
        
        if (arg_id1 < prd_id1) posit += "0";
        else if (arg_id1 > prd_id1) posit += "1";
        else posit += "2";

        if (arg_id1 < prd_id2) posit += "0";
        else if (arg_id1 > prd_id2) posit += "1";
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
                    
                    if (!"".equals(f.compound_noun))
                        feature[k++] = "8" + f.compound_noun + caseLabel;

                    if (!"".equals(f.compound_joshi))
                        feature[k++] = "9" + f.compound_joshi + caseLabel;
                }

                if (i==1) {
                    feature[k++] = "10" + f.case_alter + caseLabel;

                    if (!"".equals(f.compound_sahen_noun)) {
                        feature[k++] = "11" + f.compound_sahen_noun + caseLabel;
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
    
    private ArrayList getPhiId(String[] feature) {
        ArrayList<Integer> featureId = new ArrayList();
        for(int i=0; i<feature.length; ++i) {
            if (feature[i] == null) break;
            featureId.add((feature[i].hashCode() >>> 1) % weightSize);
        }
        return featureId;
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

                if (!"".equals(phi1.compound_noun))
                    feature[phiIndex++] = "32" + phi1.compound_noun + chead2.R_FORM + caseLabel;

                if (!"".equals(phi2.compound_sahen_noun))
                    feature[phiIndex++] = "33" + chead1.FORM + phi2.compound_sahen_noun + caseLabel;
                
                if (!"".equals(phi1.compound_joshi)) {
                    feature[phiIndex++] = "34" + phi1.compound_joshi + chead2.R_FORM + caseLabel;
                    feature[phiIndex++] = "35" + phi1.compound_joshi + phi2.compound_sahen_noun + caseLabel;

                    feature[phiIndex++] = "36" + phi1.compound_joshi + phi2.case_alter + caseLabel;
                    feature[phiIndex++] = "37" + phi1.compound_joshi + phi2.compound_sahen_noun + phi2.case_alter + caseLabel;
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
            String label = phi2.case_alter + caseLabel;

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
            feature[phiIndex++] = "31" + word1.FORM + "_" + word2.R_FORM + "_" + caseLabel;
            
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

        int nextArgIndex = phiWindow[0].INDEX + 1;
        if (nextArgIndex > -1 && nextArgIndex < words.size() && nextArgIndex != phiWindow[1].INDEX)
            phiWindow[2] = words.get(nextArgIndex);

        return phiWindow;
    }
    
}

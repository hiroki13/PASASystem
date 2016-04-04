/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author hiroki
 */

public class Feature implements Serializable{
    //[case][case][prd][prd][arg][arg]
    public ArrayList[][][][][][] cache;
    public List<Sentence> sentencelist;
    public int case_length;
    public int w;
    
    public Feature(int c_length) {
        case_length = c_length;
    }
    
    final public ArrayList<Integer> extractFeature(final Sentence sentence,
                                                     final int arg_id,
                                                     final int prd_id,
                                                     final int case_label){
        final ArrayList<Integer> usedFeatures = new ArrayList<>();
        final Chunk[] feats = makePhi(arg_id, prd_id, sentence);

        // Unigram feature
        usedFeatures.addAll(uniFeature(feats, case_label));
        
        // Combinatorial features
        usedFeatures.addAll(biFeature(feats, sentence, case_label));
            
        return usedFeatures;
    }


    final public ArrayList<Integer> extractFeature(final Sentence sentence,
                                                     final int[][] graph){
        final ArrayList<Integer> usedFeatures = new ArrayList<>();

        final ArrayList<Chunk> chunks = sentence.chunks;
        final ArrayList<Integer> args = sentence.arg_indices;
        final ArrayList<Integer> prds = sentence.prd_indices;
        final int prd_length = prds.size();
        
        final int[][] t_graph = genTGraph(prd_length, graph); // tenchi
        
        for (int case_label1=0; case_label1<case_length; case_label1++) {
            final int[] tmp_graph1 = t_graph[case_label1];
            final ArrayList[][][][][] tmp_cache1 = cache[case_label1];
            
            for (int case_label2=case_label1; case_label2<case_length; case_label2++) {
                final int[] tmp_graph2 = t_graph[case_label2];
                final ArrayList[][][][] tmp_cache2 = tmp_cache1[case_label2];
                
                usedFeatures.addAll(extractFeature(sentence, tmp_graph1,
                                                   tmp_graph2, case_label1,
                                                   case_label2, chunks,
                                                   args, prds, prd_length,
                                                   tmp_cache2));
            }
        }

        return usedFeatures;
    }
    
    final private int[][] genTGraph(final int prd_length, final int[][] graph) {
        final int[][] t_graph = new int[case_length][prd_length];
        for (int prd_i=0; prd_i<prd_length; prd_i++) {
            for (int case_label=0; case_label<case_length; case_label++) {
                t_graph[case_label][prd_i] = graph[prd_i][case_label];
            }
        }
        return t_graph;
    }
        
    final private ArrayList<Integer> extractFeature(final Sentence sentence,
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
                    arg_path1 = sentence.dep_path[a1][prd_id1];
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
                    final String prd_path = sentence.dep_path[prd_id1][prd_id2];
                    final String arg_path2 = sentence.dep_path[a1][prd_id2];
                                                            
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
                    tmp_feature.add((f.hashCode() >>> 1) % w);
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
        final int arg_length = sentence.arg_indices.size();
        int k=0;
        final ArrayList<Chunk> chunks = sentence.chunks;
        final ArrayList<Integer> prd_ids = sentence.prd_indices;
        final ArrayList<Integer> arg_ids = sentence.arg_indices;
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
            usedFeatures.add((f.hashCode() >>> 1) % w);
        }
        
        return usedFeatures;
    }
            
    
    final private Chunk[] makePhi(final int arg_id, final int prd_id,
                                   final Sentence sentence) {
        final Chunk[] feats = new Chunk[3];
        feats[0] = (Chunk) sentence.chunks.get(arg_id);
        feats[1] = (Chunk) sentence.chunks.get(prd_id);

        final int n1 = feats[0].index+1;

        if (n1 > -1 && n1 < sentence.size() && n1 != feats[1].index)
            feats[2] = (Chunk) sentence.chunks.get(n1);

        return feats;
    }
    
    final private String direction(final int prd_id1, final int prd_id2) {
        if (prd_id1 < prd_id2) return "L";
        else if (prd_id1 > prd_id2) return "R";
        else return "N";
    }

    final private String depInfo(String dep) {
        if ("0".equals(dep)) return "A";
        else if ("1".equals(dep)) return "B";
        else if ("00".equals(dep)) return "C";
        else if ("01".equals(dep)) return "D";
        else if ("10".equals(dep)) return "E";
        else if ("11".equals(dep)) return "F";
        else return "G";
    }

    final private String[] position(final int arg_id1, final int arg_id2,
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

    
    final private String position_coarg(final int arg_id1, final int prd_id1,
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

    final private String dist(final int id1, final int id2,
                              final int arg_length) {
        if (id1 == arg_length) return "N1";
        else if (id2 == arg_length) return "N2";
        
        final int dist = id2 -id1;

        if (dist == 1) return "A";
        else if (dist >= 2 && dist <= 5) return "B";
        else if (dist > 6) return "C";
        else if (dist == -1) return "D";
        else if (dist <= -2 && dist >= -5) return "E";
        else if (dist < -6) return "F";
        else return "G";
    }
    
        
    final private ArrayList uniFeature(final Chunk[] feats, final int c_label) {
        final ArrayList usedFeatures = new ArrayList<>();
        final String[] feature = new String[30];
        int k = 0;
        
        for(int i=0; i<3; ++i){
            if (feats[i] == null) continue;

            final Chunk f = feats[i];
            final Token chead = f.chead;
            
            if (chead != null) {
                feature[k++] = "1" + i + chead.r_form + c_label;                
                feature[k++] = "2" + i + chead.cpos + c_label;
                feature[k++] = "3" + i + chead.pos + c_label;
                
                if (i > 1) continue;
                
                feature[k++] = "4" + i + chead.form + c_label;

                if (!"*".equals(chead.inf_type))
                    feature[k++] = "5" + i + chead.inf_type + c_label;
                if (!"*".equals(chead.inf_form))
                    feature[k++] = "6" + i + chead.inf_form + c_label;
                
                if (i==0) {
                    feature[k++] = "7" + f.aux + c_label;
                    
                    if (!"".equals(f.compound_noun))
                        feature[k++] = "8" + f.compound_noun + c_label;

                    if (!"".equals(f.compound_joshi))
                        feature[k++] = "9" + f.compound_joshi + c_label;
                }

                if (i==1) {
                    feature[k++] = "10" + f.case_alter + c_label;

                    if (!"".equals(f.compound_sahen_noun)) {
                        feature[k++] = "11" + f.compound_sahen_noun + c_label;
                    }
                }               
            }
        }

        // Feature ID registration
        for(int i=0; i<feature.length; ++i) {
            if (feature[i] == null) continue;
            String f = feature[i];
            usedFeatures.add((f.hashCode() >>> 1) % w);
        }
                
        return usedFeatures;        
    }
    
    
    final private ArrayList biFeature(final Chunk[] feats,
                                       final Sentence sentence,
                                       final int c_label) {
        final ArrayList usedFeatures = new ArrayList<>();
        final String[] feature = new String[30];
        int k = 0;

        final Chunk f1 = feats[0];
        final Chunk f2 = feats[1];
        
        if (f1 != null && f2 != null) {
            final Token chead1 = f1.chead;
            final Token chead2 = f2.chead;
            
            if (chead1 != null && chead2 != null) {
                feature[k++] = "31" + chead1.form + chead2.r_form + c_label;

                if (!"".equals(f1.compound_noun))
                    feature[k++] = "32" + f1.compound_noun + chead2.r_form
                                    + c_label;

                if (!"".equals(f2.compound_sahen_noun))
                    feature[k++] = "33" + chead1.form + f2.compound_sahen_noun
                                    + c_label;
                
                if (!"".equals(f1.compound_joshi)) {
                    feature[k++] = "34" + f1.compound_joshi
                                    + chead2.r_form + c_label;
                    feature[k++] = "35" + f1.compound_joshi
                                    + f2.compound_sahen_noun + c_label;

                    feature[k++] = "36" + f1.compound_joshi 
                                    + f2.case_alter + c_label;
                    feature[k++] = "37" + f1.compound_joshi
                                    + f2.compound_sahen_noun
                                    + f2.case_alter + c_label;
                }
            }
            
            // Distance
            int dist = f2.index - f1.index;
            String pre_post;
            
            if (dist > 0) pre_post = "PRE";
            else pre_post = "POST";

            feature[k++] = "41_" + pre_post + c_label;
            feature[k++] = "42_" + pre_post + dist + c_label;
            
            // Dependency Path
            String label = f2.case_alter + c_label;

            final int arg_id;
            if (f1.index > -1)
                arg_id = f1.index;
            else
                arg_id = sentence.size()-1;
            final int prd_id = f2.index;

            final int dep_dist = sentence.dep_dist[arg_id][prd_id];
            final String dpath = sentence.dep_path[arg_id][prd_id] + label;
            final String dpath0 = sentence.dep_pos_path[arg_id][prd_id] + label;
            final String dpath1 = sentence.dep_verb_path[arg_id][prd_id] + label;
            final String dpath2 = sentence.dep_rform_path[arg_id][prd_id] + label;            

            
            feature[k++] = "43" + dep_dist + c_label;
            
            feature[k++] = "44" + dpath;
            feature[k++] = "45" + dpath0;
            feature[k++] = "46" + dpath1;
            feature[k++] = "47" + dpath2;

            String p_form = f2.reg_form;
            String a_form = f1.reg_form;
            
            feature[k++] = "51" + dpath + p_form;
            feature[k++] = "52" + dpath1 + p_form;
            feature[k++] = "53" + dpath2 + p_form;

            feature[k++] = "54" + dpath + a_form;
            feature[k++] = "55" + dpath1 + a_form;
            feature[k++] = "56" + dpath2 + a_form;
            
            feature[k++] = "57" + dpath + a_form + p_form;
            feature[k++] = "58" + dpath1 + a_form + p_form;
            feature[k++] = "59" + dpath2 + a_form + p_form;
            
        }
        
        // Feature ID registration
        for(int i=0; i<feature.length; ++i) {
            if (feature[i] == null) continue;
            String f = feature[i];
            usedFeatures.add((f.hashCode() >>> 1) % w);
        }
        
        return usedFeatures;
    }
    
}

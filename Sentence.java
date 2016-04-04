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

final public class Sentence implements Serializable{
    public int index;  // index within the dataset
    public int ntc_id;  // predefined id in NAIST Text Corpus

    public ArrayList chunks = new ArrayList();

    // arg_indces contain only the index of the chunk that has a head word
    public ArrayList<Integer> arg_indices = new ArrayList<>();
    public ArrayList<Integer> prd_indices = new ArrayList<>();
    
    // o_args contains the predefined indices of chunks in this sentence
    public int[] o_args;

    // o_graph contains the arg_indices
    public int[][] o_graph;
    
    // whether this sentence contains any preds
    public boolean has_prds;

    // total number of dep/zero case args
    public float[] n_dep_case_args;
    public float[] n_zero_case_args;

    // dep info used for features
    int[][] dep_dist;
    String[][] dep_path;
    String[][] dep_pos_path;
    String[][] dep_verb_path;
    String[][] dep_rform_path;
    String[][] dep_aux_path;
    String[][] dep_joshi_path;

    // number of cases to be parsed
    public int n_cases;

    
    public Sentence(int index, int n_cases){        
        this.index = index;
        this.n_cases = n_cases;
        this.n_dep_case_args = new float[n_cases];
        this.n_zero_case_args = new float[n_cases];
    }

    final public void add(Chunk chunk){
        this.chunks.add(chunk);
    }
    
    final public int size(){
        return this.chunks.size();
    }
    
    final public void setParsedCases(int case_label) {
        for (int i=0; i<this.size()-1; ++i) {
            Chunk chunk = (Chunk) chunks.get(i);
            chunk.setParsedCase(case_label);
        }
    }

    final public void setParsedCases(int[] case_labels) {
        for (int i=0; i<this.size()-1; ++i) {
            Chunk chunk = (Chunk) chunks.get(i);
            chunk.setParsedCase(case_labels);
        }
    }
    
    final public void setArgCandidates() {
        for (int i=0; i<this.size(); ++i) {
            Chunk chunk = (Chunk) chunks.get(i);
            if (chunk.chead != null) {
                this.arg_indices.add(i);
            }
        }        
    }
    
    final public void setPrds() {
        Chunk chunk;

        for (int i=0; i<chunks.size(); ++i) {
            chunk = (Chunk) chunks.get(i);

            if (chunk.pred && chunk.chead != null) {
                this.has_prds = true;
                this.prd_indices.add(i);
            }
        }
    }
    
    final public void setOracleArgs() {
        int n_prds = this.prd_indices.size();
        this.o_args = new int[this.size()];
        this.o_graph = new int[n_prds][this.n_cases];

        for (int i=0; i<this.o_args.length; ++i)
            o_args[i] = -1;

        for (int prd_i=0; prd_i<n_prds; ++prd_i) {
            Chunk prd = (Chunk) chunks.get((int) this.prd_indices.get(prd_i));

            if (prd.chead != null && prd.pred) {
                for (int case_label=0; case_label<this.n_cases; case_label++)
                    setOracleCaseArg(prd_i, prd, case_label);
            }
        }
    }
/*
    final private void setOracleCaseArg(int prd_i, Chunk prd, int case_label) {
        int[] oracle_dep_arg;
        int[] oracle_zero_arg;
 
        if (case_label == 0) {
            oracle_dep_arg = prd.ga;
            oracle_zero_arg = prd.zero_ga;
        }
        else if (case_label == 1) {
            oracle_dep_arg = prd.o;            
            oracle_zero_arg = prd.zero_o;
        }
        else {
            oracle_dep_arg = prd.ni;                        
            oracle_zero_arg = prd.zero_ni;
        }
        
        boolean null_dep = hasNoOracle(prd_i, oracle_dep_arg, case_label);
        boolean null_zero = hasNoOracle(prd_i, oracle_zero_arg, case_label);

        if (null_dep && null_zero) {
            int oracle_arg_id = this.size()-1; // indicating the "NULL" node            
            o_args[prd_i] = oracle_arg_id;                            
            o_graph[prd_i][case_label] = this.arg_indices.indexOf(oracle_arg_id);
            
        }
    }
*/
    final private void setOracleCaseArg(int prd_i, Chunk prd, int case_label) {
        final int[] oracle_dep_arg = prd.parsed_cases[case_label];
        final int[] oracle_zero_arg = prd.parsed_zero_cases[case_label];

        boolean null_dep = hasNoOracle(prd_i, oracle_dep_arg, case_label);
        boolean null_zero = hasNoOracle(prd_i, oracle_zero_arg, case_label);

        if (null_dep && null_zero) {
            int oracle_arg_id = this.size()-1; // indicating the "NULL" node            
            o_args[prd_i] = oracle_arg_id;                            
            o_graph[prd_i][case_label] = this.arg_indices.indexOf(oracle_arg_id);
            
        }
    }

    final private boolean hasNoOracle(int prd_i, int[] oracle_args,
                                        int case_label) {
        for (int i=0; i<oracle_args.length; ++i) {        
            int oracle_arg_id = oracle_args[i];

            if (oracle_arg_id < 0) return true;          
                    
            Chunk arg = (Chunk) this.chunks.get(oracle_arg_id);
            
            if (arg.chead == null) continue;
                                
            this.o_args[prd_i] = oracle_arg_id;
            this.o_graph[prd_i][case_label] = this.arg_indices.indexOf(oracle_arg_id);            

            break;
        }

        return false;
    }
        
    final public void setDeps() {
        final int sent_length = this.size();
        
        dep_dist = new int[sent_length][sent_length];
        dep_path = new String[sent_length][sent_length];
        dep_pos_path = new String[sent_length][sent_length];
        dep_verb_path = new String[sent_length][sent_length];
        dep_rform_path = new String[sent_length][sent_length];
        dep_aux_path = new String[sent_length][sent_length];
        dep_joshi_path = new String[sent_length][sent_length];

        for (int i=0; i<this.size(); ++i) {
            final Chunk chunk1 = (Chunk) chunks.get(i);
            
            int[] tmp_dep_dist = dep_dist[i];
            String[] tmp_dep_path = dep_path[i];
            String[] tmp_dep_pos_path = dep_pos_path[i];
            String[] tmp_dep_verb_path = dep_verb_path[i];
            String[] tmp_dep_rform_path = dep_rform_path[i];
            String[] tmp_dep_aux_path = dep_aux_path[i];
            String[] tmp_dep_joshi_path = dep_joshi_path[i];
            
            for (int j=0; j<this.size(); ++j) {
                Chunk chunk2 = (Chunk) chunks.get(j);
        
                ArrayList path = getDependencyPath(chunk1, chunk2);
                if (path.isEmpty())
                    return;
                String dep_path = getDepPathPhi(path);
                String[] dep_info_path = getDepInfoPathPhi(path);
                
                tmp_dep_dist[j] = path.size()-1;
                tmp_dep_path[j] = dep_path;
                tmp_dep_pos_path[j] = dep_info_path[0];
                tmp_dep_verb_path[j] = dep_info_path[1];
                tmp_dep_rform_path[j] = dep_info_path[2];
                tmp_dep_aux_path[j] = dep_info_path[3];
                tmp_dep_joshi_path[j] = dep_info_path[4];
            }
        }
    }
    
    final private ArrayList getDependencyPath(final Chunk chunk1,
                                                final Chunk chunk2) {
        int arg_id = chunk1.index;
        int prd_id = chunk2.index;
        
        if (arg_id < 0 || prd_id < 0) {
            ArrayList NULL = new ArrayList<>();
            NULL.add(-2);
            return NULL;
        }
        else if (arg_id == prd_id) {
            ArrayList NULL = new ArrayList<>();
            NULL.add(-1);
            return NULL;            
        }
        
        ArrayList path1 = searchRootPath(arg_id, new ArrayList<>());
        ArrayList path2 = searchRootPath(prd_id, new ArrayList<>());
        return joinTwoPath(path1, path2);
    }
    
    final private ArrayList searchRootPath(final int chunk_id,
                                             final ArrayList path) {
        if (chunk_id < 0) {
            ArrayList NULL = new ArrayList<>();
            NULL.add(-1);
            return NULL;
        }
        Chunk chunk = (Chunk) chunks.get(chunk_id);
        path.add(chunk.index);
        int head = chunk.head;
        if (head == -1) return path;
        return searchRootPath(head, path);
    }
    
    final private ArrayList<Integer> joinTwoPath(final ArrayList<Integer> arg_path,
                                                  final ArrayList<Integer> prd_path) {
        ArrayList<Integer> root = new ArrayList<>();
        
        int arg_id;
        int prd_id;
        
        for (int i=0; i<arg_path.size(); ++i) {
            arg_id = arg_path.get(i);
            
            for (int j=0; j<prd_path.size(); ++j) {
                prd_id = prd_path.get(j);

                if (arg_id == prd_id) {
                    for (int k=0; k<i+1; ++k) root.add(arg_path.get(k));
                    for (int k=j-1; k>-1; --k) root.add(prd_path.get(k));
                    return root;
                }
            }
        }
        return root;
    }
    
    final private String getDepPathPhi(final ArrayList<Integer> path) {
        String dep_path = "";
        int node = path.get(0);
        int tmp_node;
        
        if (node == -2) return "NULL";
        else if (node == -1) return "SAME";
        
        for (int i=1; i<path.size(); ++i) {
            tmp_node = path.get(i);
            if (tmp_node > node) dep_path += "0";
            else dep_path += "1";
            node = tmp_node;
        }
        return dep_path;
    }

    final private String[] getDepInfoPathPhi(final ArrayList<Integer> path) {
        String dep_pos_path = "";
        String dep_verb_path = "";
        String dep_r_path = "";
        String dep_aux_path = "";
        String dep_joshi_path = "";
        
        int node = path.get(0);
        int tmp_node;
        String pos;
        String joshi;
        String verb;
        String sahen;
        String regular;
        String direct;
        
        if (node == -2) return new String[]{"NULL","NULL","NULL","NULL","NULL"};
        else if (node == -1) return new String[]{"SAME","SAME","SAME","SAME","SAME"};        
        
        for (int i=1; i<path.size(); ++i) {
            tmp_node = path.get(i);
            Chunk chunk = (Chunk) chunks.get(tmp_node);

            if (chunk.chead != null) pos = chunk.chead.cpos;
            else pos = "NULL";

            joshi = chunk.compound_joshi;
            
            verb = "NULL";
            sahen = "";
            if (chunk.chead != null) {
                if (chunk.sahen_verb) {
                    for (int j=0; j<chunk.sahen_noun.size(); ++j) {
                        sahen += ((Token) chunk.sahen_noun.get(j)).form;
                    }
                }
                else if (chunk.verb) verb = chunk.chead.r_form;
                else sahen = "NULL";
            }
            else sahen = "NULL";
            
            regular = "NULL";
            if (chunk.chead != null) {
                if (chunk.verb) regular = chunk.chead.r_form;
                else regular = chunk.chead.form;
            }
                                               
            if (tmp_node > node) direct = "0";
            else direct = "1";

            dep_pos_path += direct + pos;
            dep_verb_path += direct + sahen + verb + joshi;
            dep_r_path += direct + regular + joshi;
            dep_aux_path += direct + chunk.aux;
            dep_joshi_path += direct + joshi;

            node = tmp_node;
        }
        
        return new String[]{dep_pos_path, dep_verb_path, dep_r_path, dep_aux_path, dep_joshi_path};
    }
    
    final public void setTotalNumCaseArgs() {
        int[] parsed_cases;
        int[] parsed_zero_cases;
        int arg_dep;
        int arg_zero;
        
        for (int prd_i=0; prd_i<prd_indices.size(); ++prd_i) {      
            Chunk prd = (Chunk) chunks.get((int) prd_indices.get(prd_i));

            for (int case_i=0; case_i<prd.parsed_cases.length; case_i++) {
                parsed_cases = prd.parsed_cases[case_i];
                parsed_zero_cases = prd.parsed_zero_cases[case_i];
                
                for (int i=0; i<parsed_cases.length; i++) {
                    arg_dep = parsed_cases[i];
                    if (arg_dep > -1) n_dep_case_args[case_i] += 1.0f;
                }

                for (int i=0; i<parsed_zero_cases.length; i++) {
                    arg_zero = parsed_zero_cases[i];
                    if (arg_zero > -1) n_zero_case_args[case_i] += 1.0f;
                }

            }
        }        
    }
    
}

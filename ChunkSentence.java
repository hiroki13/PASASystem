
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

final public class ChunkSentence extends Sentence {

    public ChunkSentence(int index, int nCases) {        
        this.index = index;
        this.nCases = nCases;
        this.nDepCaseArgs = new float[nCases];
        this.nZeroCaseArgs = new float[nCases];
    }

    @Override
    final public void add(Chunk chunk){
        chunks.add(chunk);
    }
    
    @Override
    final public int size(){
        return chunks.size();
    }
    
    @Override
    final public boolean hasDepCicle() {                    
        for (int i=0; i<size()-1; ++i) {
            Chunk chunk = chunks.get(i);
            if (chunk.INDEX == chunk.DEP_HEAD_INDEX)
                return true;                
        }
        return false;
    }
    
    @Override
    final public void setElemParams() {
        for (int i=0; i<size()-1; ++i)
            chunks.get(i).setParams();
    }
    
    @Override
    final public void setParams(int[] caseLabels) {
        setElemParams();
        setParsedCases(caseLabels);
        setArgs();        
        setPrds();        
        setOracleGraph();        
        setDepPaths();        
        setTotalNumCaseArgs();        
    }
    
    final public void setParsedCases(int[] caseLabels) {
        for (int i=0; i<this.size()-1; ++i)
            chunks.get(i).setParsedCase(caseLabels);
    }
    
    final public void setArgs() {
        for (int i=0; i<this.size(); ++i)
            if (chunks.get(i).chead != null) argIndices.add(i);
    }
    
    final public void setPrds() {
        for (int i=0; i<chunks.size(); ++i) {
            Chunk chunk = chunks.get(i);
            if (chunk.pred && chunk.chead != null) prdIndices.add(i);
        }
        if (prdIndices.size() > 0) hasPrds = true;
    }
    
    final public void setOracleGraph() {
        int nPrds = prdIndices.size();
        oracleGraph = new int[nPrds][nCases];

        for (int prd_i=0; prd_i<nPrds; ++prd_i) {
            Chunk prd = chunks.get(prdIndices.get(prd_i));

            if (prd.chead != null && prd.pred) {
                for (int case_label=0; case_label<nCases; case_label++)
                    setOracleCaseArg(prd_i, prd, case_label);
            }
        }
    }

    private void setOracleCaseArg(int prd_i, Chunk prd, int case_label) {
        final int[] oracleDepArg = prd.parsedCases[case_label];
        final int[] oracleZeroArg = prd.parsedZeroCases[case_label];

        boolean nullDep = hasNoOracle(prd_i, oracleDepArg, case_label);
        boolean nullZero = hasNoOracle(prd_i, oracleZeroArg, case_label);

        if (nullDep && nullZero) {
            int nullArgIndex = this.size()-1; // indicating the "NULL" node            
            oracleGraph[prd_i][case_label] = argIndices.indexOf(nullArgIndex);
        }
    }

    private boolean hasNoOracle(int prd_i, int[] oracle_args, int case_label) {
        for (int i=0; i<oracle_args.length; ++i) {        
            int oracle_arg_id = oracle_args[i];

            if (oracle_arg_id < 0) return true;          
                    
            Chunk arg = (Chunk) this.chunks.get(oracle_arg_id);
            
            if (arg.chead == null) continue;                                
            this.oracleGraph[prd_i][case_label] = this.argIndices.indexOf(oracle_arg_id);            
            break;
        }

        return false;
    }
        
    final public void setDepPaths() {
        int nChunks = this.size();
        
        depDist = new int[nChunks][nChunks];
        depPath = new String[nChunks][nChunks];
        depPosPath = new String[nChunks][nChunks];
        depVerbPath = new String[nChunks][nChunks];
        depRformPath = new String[nChunks][nChunks];
        depAuxPath = new String[nChunks][nChunks];
        depJoshiPath = new String[nChunks][nChunks];

        for (int i=0; i<this.size(); ++i) {
            final Chunk chunk1 = (Chunk) chunks.get(i);
            
            int[] tmp_dep_dist = depDist[i];
            String[] tmp_dep_path = depPath[i];
            String[] tmp_dep_pos_path = depPosPath[i];
            String[] tmp_dep_verb_path = depVerbPath[i];
            String[] tmp_dep_rform_path = depRformPath[i];
            String[] tmp_dep_aux_path = depAuxPath[i];
            String[] tmp_dep_joshi_path = depJoshiPath[i];
            
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
    
    private ArrayList getDependencyPath(final Chunk chunk1, final Chunk chunk2) {
        int arg_id = chunk1.INDEX;
        int prd_id = chunk2.INDEX;
        
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
    
    private ArrayList searchRootPath(final int chunk_id, final ArrayList path) {
        if (chunk_id < 0) {
            ArrayList NULL = new ArrayList<>();
            NULL.add(-1);
            return NULL;
        }
        Chunk chunk = (Chunk) chunks.get(chunk_id);
        path.add(chunk.INDEX);
        int head = chunk.DEP_HEAD_INDEX;
        if (head == -1) return path;
        return searchRootPath(head, path);
    }
    
    private ArrayList<Integer> joinTwoPath(final ArrayList<Integer> arg_path,
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
    
    private String getDepPathPhi(final ArrayList<Integer> path) {
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

    private String[] getDepInfoPathPhi(final ArrayList<Integer> path) {
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

            if (chunk.chead != null) pos = chunk.chead.CPOS;
            else pos = "NULL";

            joshi = chunk.compound_joshi;
            
            verb = "NULL";
            sahen = "";
            if (chunk.chead != null) {
                if (chunk.sahen_verb) {
                    for (int j=0; j<chunk.sahen_noun.size(); ++j) {
                        sahen += ((Word) chunk.sahen_noun.get(j)).FORM;
                    }
                }
                else if (chunk.verb) verb = chunk.chead.R_FORM;
                else sahen = "NULL";
            }
            else sahen = "NULL";
            
            regular = "NULL";
            if (chunk.chead != null) {
                if (chunk.verb) regular = chunk.chead.R_FORM;
                else regular = chunk.chead.FORM;
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
        
        for (int prd_i=0; prd_i<prdIndices.size(); ++prd_i) {      
            Chunk prd = (Chunk) chunks.get((int) prdIndices.get(prd_i));

            for (int case_i=0; case_i<prd.parsedCases.length; case_i++) {
                parsed_cases = prd.parsedCases[case_i];
                parsed_zero_cases = prd.parsedZeroCases[case_i];
                
                for (int i=0; i<parsed_cases.length; i++) {
                    arg_dep = parsed_cases[i];
                    if (arg_dep > -1) nDepCaseArgs[case_i] += 1.0f;
                }

                for (int i=0; i<parsed_zero_cases.length; i++) {
                    arg_zero = parsed_zero_cases[i];
                    if (arg_zero > -1) nZeroCaseArgs[case_i] += 1.0f;
                }

            }
        }        
    }
    
}

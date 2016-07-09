
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
    final public void setParams() {
        setElemParams();
        setArgs();        
        setPrds();
        setWordDeps();
        setDepPaths();
        setCaseArgIndex();
        setParsedCases();
        setOracleGraph();
        setCaseStatistics();
    }
    
    private void setParsedCases() {
        for (int i=0; i<chunks.size(); ++i) {
            Chunk chunk = chunks.get(i);
            chunk.parsedDepCases = new int[nCases];
            chunk.parsedZeroCases = new int[nCases];
            chunk.parsedDepCases[0] = chunk.ga;        
            chunk.parsedZeroCases[0] = chunk.zeroGa;
            chunk.parsedDepCases[1] = chunk.o;        
            chunk.parsedZeroCases[1] = chunk.zeroO;
            chunk.parsedDepCases[2] = chunk.ni;        
            chunk.parsedZeroCases[2] = chunk.zeroNi;
        }
    }

    private void setCaseArgIndex() {
        for (int i=0; i<words.size(); ++i) {
            Word word = words.get(i);
            if (word.IS_PRD) word.setCaseArgIndex(this);
        }
        
        for (int i=0; i<chunks.size(); ++i) {
            Chunk chunk = chunks.get(i);
 
            if (chunk.hasPrd) {
                Word prd = chunk.prd;
                if (prd.ga > -1)
                    chunk.ga = words.get(prd.ga).CHUNK.INDEX;        
                if (prd.o > -1)
                    chunk.o = words.get(prd.o).CHUNK.INDEX;        
                if (prd.ni > -1)
                    chunk.ni = words.get(prd.ni).CHUNK.INDEX;        
                if (prd.zeroGa > -1)
                    chunk.zeroGa = words.get(prd.zeroGa).CHUNK.INDEX;        
                if (prd.zeroO > -1)
                    chunk.zeroO = words.get(prd.zeroO).CHUNK.INDEX;        
                if (prd.zeroNi > -1)
                    chunk.zeroNi = words.get(prd.zeroNi).CHUNK.INDEX;        
            }
        }
    }

    @Override
    final public void setElemParams() {
        for (int i=0; i<size()-1; ++i)
            chunks.get(i).setParams(this, nCases);
    }
        
    final public void setArgs() {
        for (int i=0; i<this.size(); ++i)
            argIndices.add(i);
    }
    
    final public void setPrds() {
        for (int i=0; i<chunks.size(); ++i)
            if (chunks.get(i).hasPrd) prdIndices.add(i);
        if (prdIndices.size() > 0) hasPrds = true;
    }
    
    final public void setWordDeps() {
        for (int i=0; i<chunks.size()-1; ++i) {
            Chunk chunk = chunks.get(i);
            ArrayList<Word> chunkWords = chunk.words;
            int chunkHead = chunk.DEP_HEAD_INDEX;

            for (int j=0; j<chunkWords.size()-1; ++j) {
                Word word = chunkWords.get(j);
                word.depHeadIndex = word.INDEX + 1;
            }
            
            Word headWord = chunkWords.get(chunkWords.size()-1);
            if (chunkHead == -1)
                headWord.depHeadIndex = -1;
            else {
                Chunk nextChunk = chunks.get(i+1);
                Word nextHeadWord = nextChunk.words.get(nextChunk.words.size()-1);
                headWord.depHeadIndex = nextHeadWord.INDEX;
            }
        }
    }
    
    final public void setOracleGraph() {
        int nPrds = prdIndices.size();
        oracleGraph = new int[nPrds][];

        for (int prd_i=0; prd_i<nPrds; ++prd_i) {
            Chunk prd = chunks.get(prdIndices.get(prd_i));                
            oracleGraph[prd_i] = getOracleCaseArgs(prd);
        }
    }

    private int[] getOracleCaseArgs(Chunk prd) {
        int[] graph = new int[nCases];
        int[] oracleDepArgs = prd.parsedDepCases;
        int[] oracleZeroArgs = prd.parsedZeroCases;

        for (int caseLabel=0; caseLabel<nCases; ++caseLabel)        
            graph[caseLabel] = argIndices.indexOf(argIndices.size()-1);                 

        for (int caseLabel=0; caseLabel<nCases; ++caseLabel) {        
            int oracleDepArgIndex = oracleDepArgs[caseLabel];
            int oracleZeroArgIndex = oracleZeroArgs[caseLabel];

            if (oracleDepArgIndex > -1 && chunks.get(oracleDepArgIndex).chead != null)
                graph[caseLabel] = argIndices.indexOf(oracleDepArgIndex);            
            
            if (oracleZeroArgIndex > -1 && chunks.get(oracleZeroArgIndex).chead != null)
                graph[caseLabel] = argIndices.indexOf(oracleZeroArgIndex);            
        }
        return graph;
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

            joshi = chunk.compoundJoshi;
            
            verb = "NULL";
            sahen = "";
            if (chunk.chead != null) {
                if (chunk.sahenVerb) {
                    for (int j=0; j<chunk.sahenNoun.size(); ++j) {
                        sahen += ((Word) chunk.sahenNoun.get(j)).FORM;
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
    
    @Override
    final public void setCaseStatistics() {
        int[][] cases = new int[][]{{0, 0, 0}, {0, 0, 0}, {0, 0, 0}};
        for (int i=0; i<chunks.size(); ++i) {
            Chunk chunk = chunks.get(i);
            if (chunk.hasPrd) {
                if (chunk.ga > -1) {
                    cases[0][0]++;
                    nDepCaseArgs[0]++;
                }
                else if (chunk.zeroGa > -1) {
                    cases[0][1]++;
                    nZeroCaseArgs[0]++;
                }

                if (chunk.o > -1) {
                    cases[1][0]++;
                    nDepCaseArgs[1]++;
                }
                else if (chunk.zeroO > -1) {
                    cases[1][1]++;
                    nZeroCaseArgs[1]++;
                }

                if (chunk.ni > -1) {
                    cases[2][0]++;
                    nDepCaseArgs[2]++;
                }
                else if (chunk.zeroNi > -1) {
                    cases[2][1]++;
                    nZeroCaseArgs[2]++;
                }
            }
        }
        caseStatistics = cases;
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
    final public void add(Chunk chunk){
        chunks.add(chunk);
    }
    
    @Override
    final public int size(){
        return chunks.size();
    }
        
}

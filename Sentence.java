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

public class Sentence implements Serializable{
    // number of cases to be parsed
    final public int N_CASES = Config.N_CASES;

    final public int INDEX;  // index within the dataset

    public ArrayList<Word> words = new ArrayList();
    public ArrayList<Chunk> chunks = new ArrayList();
    public ArrayList<Chunk> prds = new ArrayList();

    // argIndces contains only the index of the chunk that has a head word
    public ArrayList<Integer> argIndices = new ArrayList();
    public ArrayList<Integer> prdIndices = new ArrayList();
    
    // oracleGraph contains the arg_indices: 1D: nPrds, 2D: nCases
    public int[][] oracleGraph;
    
    // whether this sentence contains any preds
    public boolean hasPrds;

    // total number of dep/zero case args
    public float[] nDepCaseArgs, nZeroCaseArgs;

    // dep info used for features
    int[][] depDist;
    String[][] depPath, depPosPath, depVerbPath, depRformPath, depAuxPath, depJoshiPath;

    
    // number of each case type
    public int[][] caseStatistics;
    
    public Sentence(int index) {        
        this.INDEX = index;
        this.nDepCaseArgs = new float[N_CASES];
        this.nZeroCaseArgs = new float[N_CASES];
    }

    final public void setParams() {
        setArgs();        
        setPrds();
        setDepPaths();
        setCaseArgIndex();
        setParsedCases();
        setOracleGraph();
        setCaseStatistics();
    }
    
    final public void setArgs() {
        for (int i=0; i<this.size(); ++i)
            argIndices.add(i);
    }
    
    final public void setPrds() {
        for (int i=0; i<chunks.size(); ++i)
            if (chunks.get(i).hasPrd) {
                prdIndices.add(i);
                prds.add(chunks.get(i));
            }
        if (prdIndices.size() > 0)
            hasPrds = true;
    }
    
    private void setCaseArgIndex() {
        for (int i=0; i<words.size(); ++i) {
            Word word = words.get(i);
            if (word.IS_PRD)
                word.setCaseArgIndex(this);
        }
        
        for (int i=0; i<chunks.size(); ++i) {
            Chunk chunk = chunks.get(i);
 
            if (chunk.hasPrd) {
                Word prd = chunk.prd;
                if (prd.ga > -1)
                    chunk.ga = words.get(prd.ga).CHUNK_INDEX;        
                if (prd.o > -1)
                    chunk.o = words.get(prd.o).CHUNK_INDEX;        
                if (prd.ni > -1)
                    chunk.ni = words.get(prd.ni).CHUNK_INDEX;        
                if (prd.zeroGa > -1)
                    chunk.zeroGa = words.get(prd.zeroGa).CHUNK_INDEX;        
                if (prd.zeroO > -1)
                    chunk.zeroO = words.get(prd.zeroO).CHUNK_INDEX;        
                if (prd.zeroNi > -1)
                    chunk.zeroNi = words.get(prd.zeroNi).CHUNK_INDEX;        
            }
        }
    }

    private void setParsedCases() {
        for (int i=0; i<chunks.size(); ++i) {
            Chunk chunk = chunks.get(i);
            chunk.parsedDepCases = new int[N_CASES];
            chunk.parsedZeroCases = new int[N_CASES];
            chunk.parsedDepCases[0] = chunk.ga;        
            chunk.parsedZeroCases[0] = chunk.zeroGa;
            chunk.parsedDepCases[1] = chunk.o;        
            chunk.parsedZeroCases[1] = chunk.zeroO;
            chunk.parsedDepCases[2] = chunk.ni;        
            chunk.parsedZeroCases[2] = chunk.zeroNi;
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
        int[] graph = new int[N_CASES];
        int[] oracleDepArgs = prd.parsedDepCases;
        int[] oracleZeroArgs = prd.parsedZeroCases;

        for (int caseLabel=0; caseLabel<N_CASES; ++caseLabel)        
            graph[caseLabel] = argIndices.indexOf(argIndices.size()-1);                 

        for (int caseLabel=0; caseLabel<N_CASES; ++caseLabel) {        
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
        int head = chunk.HEAD;
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

            joshi = chunk.compoundFuncWord;
            
            verb = "NULL";
            sahen = "";
            if (chunk.chead != null) {
                if (chunk.isSahenVerb) {
                    for (int j=0; j<chunk.sahenNoun.size(); ++j) {
                        sahen += ((Word) chunk.sahenNoun.get(j)).FORM;
                    }
                }
                else if (chunk.isVerb) verb = chunk.chead.REG;
                else sahen = "NULL";
            }
            else sahen = "NULL";
            
            regular = "NULL";
            if (chunk.chead != null) {
                if (chunk.isVerb) regular = chunk.chead.REG;
                else regular = chunk.chead.FORM;
            }
                                               
            if (tmp_node > node) direct = "0";
            else direct = "1";

            dep_pos_path += direct + pos;
            dep_verb_path += direct + sahen + verb + joshi;
            dep_r_path += direct + regular + joshi;
            dep_aux_path += direct + chunk.particle;
            dep_joshi_path += direct + joshi;

            node = tmp_node;
        }
        
        return new String[]{dep_pos_path, dep_verb_path, dep_r_path, dep_aux_path, dep_joshi_path};
    }
    
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

    final public boolean hasDepCicle() {                    
        for (int i=0; i<size(); ++i) {
            Chunk chunk = chunks.get(i);
            if (chunk.INDEX == chunk.HEAD)
                return true;                
        }
        return false;
    }
    
    final public void add(Chunk chunk) {
        chunks.add(chunk);
        for (int i=0; i<chunk.size(); ++i)
            words.add(chunk.getWord(i));
    }
    
    final public Chunk getChunk(int index) {
        return chunks.get(index);
    }
    
    final public int size() {
        return chunks.size();
    }

}

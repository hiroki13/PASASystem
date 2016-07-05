/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.ArrayList;

/**
 *
 * @author hiroki
 */

final public class WordSentence extends Sentence {
    
    public WordSentence(int index, int nCases) {        
        this.index = index;
        this.nCases = nCases;
        this.nDepCaseArgs = new float[nCases];
        this.nZeroCaseArgs = new float[nCases];
    }

    @Override
    final public void add(Word word) {
        words.add(word);
    }
    
    @Override
    final public int size() {
        return words.size();
    }
    
    @Override
    final public void setCaseStatistics() {
        int[][] cases = new int[][]{{0, 0, 0}, {0, 0, 0}, {0, 0, 0}};
        for (int i=0; i<words.size(); ++i) {
            Word word = words.get(i);
            if (word.IS_PRD) {
                if (word.ga > -1) cases[0][0]++;
                else if (word.zeroGa > -1) cases[0][1]++;
                else if (word.interGa > -1) cases[0][2]++;

                if (word.o > -1) cases[1][0]++;
                else if (word.zeroO > -1) cases[1][1]++;
                else if (word.interO > -1) cases[1][2]++;

                if (word.ni > -1) cases[2][0]++;
                else if (word.zeroNi > -1) cases[2][1]++;
                else if (word.interNi > -1) cases[2][2]++;
            }
        }
        caseStatistics = cases;
    }
    
    @Override
    final public boolean hasDepCicle() {                    
        for (int i=0; i<words.size(); ++i) {
            Word word = words.get(i);
            if (word.depHeadIndex == word.INDEX)
                return true;
        }
        return false;
    }

    @Override
    final public void setParams() {
        setWordDeps();
        setDepPaths();
        setPrds();
        setArgs();
        setCaseArgIndex();
        setCaseStatistics();
    }
    
    final public void setWordDeps() {
        for (int i=0; i<chunks.size(); ++i) {
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
    
    private void setCaseArgIndex() {
        for (int i=0; i<words.size(); ++i) {
            Word word = words.get(i);
            if (word.IS_PRD) word.setCaseArgIndex(this);
        }
    }
    
    final public void setPrds() {
        for (int i=0; i<words.size(); ++i)
            if (words.get(i).IS_PRD) prdIndices.add(i);
        if (prdIndices.size() > 0) hasPrds = true;
        hasPrds = false;
    }
    
    final public void setArgs() {
        for (int i=0; i<words.size(); ++i)                
            argIndices.add(i);
    }
    
    final public void setOracleGraph() {
        int nPrds = prdIndices.size();
        oracleGraph = new int[nPrds][nCases];

        for (int prd_i=0; prd_i<nPrds; ++prd_i) {
            Word prd = words.get((int) prdIndices.get(prd_i));

            for (int case_label=0; case_label<this.nCases; case_label++)
                setOracleCaseArg(prd_i, prd, case_label);
        }
    }

    private void setOracleCaseArg(int prd_i, Word prd, int caseLabel) {
        final int[] oracle_dep_arg = prd.parsedCases[caseLabel];
        final int[] oracle_zero_arg = prd.parsedZeroCases[caseLabel];

        boolean null_dep = hasNoOracle(prd_i, oracle_dep_arg, caseLabel);
        boolean null_zero = hasNoOracle(prd_i, oracle_zero_arg, caseLabel);

        if (null_dep && null_zero) {
            int oracle_arg_id = this.size()-1; // indicating the "NULL" node            
            oracleGraph[prd_i][caseLabel] = this.argIndices.indexOf(oracle_arg_id);
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
        final int sent_length = this.size();
        
        depDist = new int[sent_length][sent_length];
        depPath = new String[sent_length][sent_length];
        depPosPath = new String[sent_length][sent_length];
        depAuxPath = new String[sent_length][sent_length];

        for (int i=0; i<this.size(); ++i) {
            int[] tmpDepDist = depDist[i];
            String[] tmpDepPath = depPath[i];
            String[] tmpDepPosPath = depPosPath[i];
            String[] tmpDepAuxPath = depAuxPath[i];
            
            for (int j=0; j<this.size(); ++j) {
                ArrayList path = getDepPath(i, j);

                if (path.isEmpty())
                    return;
                
                tmpDepDist[j] = path.size()-1;
                tmpDepPath[j] = getDepPathPhi(path);

                String[] depInfo = getDepInfoPathPhi(path);
                tmpDepPosPath[j] = depInfo[0];
                tmpDepAuxPath[j] = depInfo[1];
            }
        }
    }
    
    private ArrayList getDepPath(int word1_index, int word2_index) {
        if (word1_index < 0 || word2_index < 0) {
            ArrayList NULL = new ArrayList<>();
            NULL.add(-2);
            return NULL;
        }
        else if (word1_index == word2_index) {
            ArrayList NULL = new ArrayList<>();
            NULL.add(-1);
            return NULL;            
        }
        
        ArrayList path1 = getRootPath(word1_index, new ArrayList<>());
        ArrayList path2 = getRootPath(word2_index, new ArrayList<>());
        return joinTwoPath(path1, path2);
    }
    
    private ArrayList getRootPath(int token_index, ArrayList path) {
        if (token_index < 0) {
            ArrayList NULL = new ArrayList();
            NULL.add(-1);
            return NULL;
        }
        
        Word token = words.get(token_index);
        path.add(token.INDEX);
        int head = token.depHeadIndex;
        if (head == -1) return path;
        return getRootPath(head, path);
    }
    
    private ArrayList<Integer> joinTwoPath(ArrayList<Integer> path1, ArrayList<Integer> path2) {
        ArrayList<Integer> root = new ArrayList();        
        
        for (int i=0; i<path1.size(); ++i) {
            int word1Index = path1.get(i);
            
            for (int j=0; j<path2.size(); ++j) {
                int word2Index = path2.get(j);

                if (word1Index == word2Index) {
                    for (int k=0; k<i+1; ++k) root.add(path1.get(k));
                    for (int k=j-1; k>-1; --k) root.add(path2.get(k));
                    return root;
                }
            }
        }
        
        return root;
    }
    
    private String getDepPathPhi(final ArrayList<Integer> path) {
        int node = path.get(0);        
        if (node == -2) return "NULL";
        else if (node == -1) return "SAME";
        
        String depPath = "";
        for (int i=1; i<path.size(); ++i) {
            int tmpNode = path.get(i);
            if (tmpNode > node) depPath += "0";
            else depPath += "1";
            node = tmpNode;
        }
        return depPath;
    }

    private String[] getDepInfoPathPhi(final ArrayList<Integer> path) {
        String depPosPath = "";
        String depAuxPath = "";
        
        int node = path.get(0);
        String direct;
        
        if (node == -2) return new String[]{"NULL","NULL"};
        else if (node == -1) return new String[]{"SAME","SAME"};        
        
        for (int i=1; i<path.size(); ++i) {
            int tmpNode = path.get(i);
            Word token = words.get(tmpNode);
            String pos = token.CPOS;
                                                           
            if (tmpNode > node) direct = "0";
            else direct = "1";

            depPosPath += direct + pos;
            depAuxPath += direct + token.AUX;

            node = tmpNode;
        }
        
        return new String[]{depPosPath, depAuxPath};
    }
    
}

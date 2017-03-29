
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
public class Sample {
    final public int N_CASES = Config.N_CASES;
    final public Chunk[] prds, args;
    final public int NULL_ARG_INDEX;
    
    // oracleGraph 1D: nPrds, 2D: nCases
    final public int[][] oracleGraph;

    public int[] oracleFeatIDs;
    public int[] featIDs;

    final int[][] depDist;
    final String[][] depPath, depPosPath, depVerbPath, depRformPath, depAuxPath, depJoshiPath;

    public Sample(Sentence sent) {
        prds = setPrds(sent);
        args = setArgs(sent);
        NULL_ARG_INDEX = args.length-1;
        depDist = new int[prds.length][args.length];
        depPath = new String[prds.length][args.length];
        depPosPath = new String[prds.length][args.length];
        depVerbPath = new String[prds.length][args.length];
        depRformPath = new String[prds.length][args.length];
        depAuxPath = new String[prds.length][args.length];
        depJoshiPath = new String[prds.length][args.length];
        setDepPaths();
        oracleGraph = setOracleGraph();
    }
    
    final public int getPrdIndex(int prdGraphIndex) {
        return prds[prdGraphIndex].INDEX;
    }
    
    final public int getArgIndex(int argGraphIndex) {
        return args[argGraphIndex].INDEX;
    }
    
    private Chunk[] setPrds(Sentence sent) {
        ArrayList<Integer> prdIndices = new ArrayList();
        for (int prdIndex=0; prdIndex<sent.prds.length; ++prdIndex) {
            Word prd = sent.prds[prdIndex].prd;
            boolean hasArg = false;

            for (int caseLabel=0; caseLabel<N_CASES; ++caseLabel) {
                for (int argType=0; argType<Word.INTER_ZERO; ++argType) {
                    if (prd.argIndices[caseLabel][argType] > -1) {
                        prdIndices.add(prdIndex);
                        hasArg = true;
                        break;
                    }
                }
                if (hasArg)
                    break;
            }
        }
        
        Chunk[] prds = new Chunk[prdIndices.size()];
        for (int i=0; i<prds.length; ++i)
            prds[i] = sent.prds[prdIndices.get(i)];
        return prds;
    }

    private Chunk[] setArgs(Sentence sent) {
        Chunk[] args = new Chunk[sent.chunks.length+1];
        for (int i=0; i<args.length-1; ++i)
            args[i] = sent.getChunk(i);
        args[args.length-1] = Chunk.getNullChunk(args.length-1);
        return args;
    }
    
    private int[][] setOracleGraph() {
        int[][] oracleGraph = genInitOracleGraph();
        for (int prdIndex=0; prdIndex<prds.length; ++prdIndex)
            for (int caseLabel=0; caseLabel<N_CASES; ++caseLabel)
                oracleGraph[prdIndex][caseLabel] = getArgIndex(prds[prdIndex], caseLabel);
        return oracleGraph;
    }
    
    private int[][] genInitOracleGraph() {
        int[][] oracleGraph = new int[prds.length][N_CASES];
        for (int i=0; i<oracleGraph.length; ++i)
            for (int j=0; j<N_CASES; ++j)
                oracleGraph[i][j] = -1;
        return oracleGraph;        
    }
    
    private int getArgIndex(Chunk prdChunk, int caseLabel) {
        int depArgIndex = prdChunk.prd.argIndices[caseLabel][Word.DEP];
        int zeroArgIndex = prdChunk.prd.argIndices[caseLabel][Word.INTRA_ZERO];

        if (depArgIndex > Word.INTER_ARG_INDEX)
            return depArgIndex;
        else if (zeroArgIndex > Word.INTER_ARG_INDEX)
            return zeroArgIndex;
        return NULL_ARG_INDEX;
    }
    
    final public void setOracleFeatIDs(FeatureExtractor featExtractor) {
        oracleFeatIDs = featExtractor.getOracleFeatIDs(this);
    }
    
    final public int sizeArgs() {
        return args.length;
    }
    
    final public int sizePrds() {
        return prds.length;
    }    

    final public static Chunk getNextChunk(Sample sample, Chunk chunk) {
        if (chunk.INDEX < sample.sizeArgs())
            return sample.args[chunk.INDEX];
        return null;
    }

    private void setDepPaths() {
        for (int prdIndex=0; prdIndex<prds.length; ++prdIndex) {
            Chunk prd = prds[prdIndex];
            
            int[] tmpDepDist = depDist[prdIndex];
            String[] tmpPath = depPath[prdIndex];
            String[] tmpPosPath = depPosPath[prdIndex];
            String[] tmpVerbPath = depVerbPath[prdIndex];
            String[] tmpRegPath = depRformPath[prdIndex];
            String[] tmpAuxPath = depAuxPath[prdIndex];
            String[] tmpJoshiPath = depJoshiPath[prdIndex];
            
            for (int argIndex=0; argIndex<args.length; ++argIndex) {
                Chunk arg = args[argIndex];
        
                ArrayList<Integer> path = getDepPath(prd, arg);
                String[] depPathFeats = getDepPathFeats(path);
                
                tmpDepDist[argIndex] = path.size()-1;
                tmpPath[argIndex] = convertDepPathString(path);
                tmpPosPath[argIndex] = depPathFeats[0];
                tmpVerbPath[argIndex] = depPathFeats[1];
                tmpRegPath[argIndex] = depPathFeats[2];
                tmpAuxPath[argIndex] = depPathFeats[3];
                tmpJoshiPath[argIndex] = depPathFeats[4];
            }
        }
    }
    
    private void setNullDepPaths() {
        for (int prdIndex=0; prdIndex<prds.length; ++prdIndex) {
            for (int argIndex=0; argIndex<args.length; ++argIndex) {
                depDist[prdIndex][argIndex] = 0;
                depPath[prdIndex][argIndex] = "NULL";
                depPosPath[prdIndex][argIndex] = "NULL";
                depVerbPath[prdIndex][argIndex] = "NULL";
                depRformPath[prdIndex][argIndex] = "NULL";
                depAuxPath[prdIndex][argIndex] = "NULL";
                depJoshiPath[prdIndex][argIndex] = "NULL";
            }
        }
    }

    private boolean hasDepCicle() {                    
        for (int i=0; i<args.length; ++i) {
            Chunk chunk = args[i];
            if (chunk.INDEX == chunk.DEP_HEAD)
                return true;                
        }
        return false;
    }
    
    private ArrayList<Integer> getDepPath(Chunk prd, Chunk arg) {
        int prdIndex = prd.INDEX;
        int argIndex = arg.INDEX;
        
        ArrayList<Integer> path = new ArrayList();
        if (argIndex == NULL_ARG_INDEX) {
            path.add(argIndex);
            return path;
        }
        else if (prdIndex == argIndex) {
            path.add(-1);
            return path;            
        }
        
        ArrayList<Integer> path1 = searchRootPath(prdIndex, new ArrayList());
        ArrayList<Integer> path2 = searchRootPath(argIndex, new ArrayList());
        return joinTwoPaths(path1, path2);
    }
    
    private ArrayList<Integer> searchRootPath(int chunkIndex, ArrayList<Integer> path) {
        if (chunkIndex < 0) {
            ArrayList<Integer> NULL = new ArrayList();
            NULL.add(-1);
            return NULL;
        }

        Chunk chunk = args[chunkIndex];
        if (path.contains(chunk.INDEX)) {
            ArrayList<Integer> NULL = new ArrayList();
            NULL.add(-1);
            return NULL;            
        }
        path.add(chunk.INDEX);

        if (chunk.DEP_HEAD < 0)
            return path;
        return searchRootPath(chunk.DEP_HEAD, path);
    }
    
    private ArrayList<Integer> joinTwoPaths(ArrayList<Integer> argPath, ArrayList<Integer> prdPath) {
        ArrayList<Integer> root = new ArrayList();
        for (int i=0; i<prdPath.size(); ++i) {
            int prdIndex = prdPath.get(i);
            
            for (int j=0; j<argPath.size(); ++j) {
                int argIndex = argPath.get(j);

                if (prdIndex == argIndex) {
                    for (int k=0; k<i+1; ++k)
                        root.add(prdPath.get(k));
                    for (int k=j-1; k>-1; --k)
                        root.add(argPath.get(k));
                    return root;
                }
            }
        }
        
        if (root.isEmpty())
            root.add(NULL_ARG_INDEX);
        return root;
    }
    
    private String convertDepPathString(ArrayList<Integer> path) {
        String depPathString = "";

        int chunkIndex = path.get(0);
        if (chunkIndex == NULL_ARG_INDEX)
            return "NULL";
        else if (chunkIndex == -1)
            return "SAME";
        
        for (int i=1; i<path.size(); ++i) {
            int tmpChunkIndex = path.get(i);
            if (tmpChunkIndex > chunkIndex)
                depPathString += "0";
            else
                depPathString += "1";
            chunkIndex = tmpChunkIndex;
        }

        return depPathString;
    }

    private String[] getDepPathFeats(ArrayList<Integer> path) {
        String posPath = "";
        String verbPath = "";
        String regPath = "";
        String auxPath = "";
        String joshiPath = "";
        
        int chunkIndex = path.get(0);
        
        if (chunkIndex == NULL_ARG_INDEX)
            return new String[]{"NULL","NULL","NULL","NULL","NULL"};
        else if (chunkIndex == -1)
            return new String[]{"SAME","SAME","SAME","SAME","SAME"};        
        
        for (int i=1; i<path.size(); ++i) {
            int tmpChunkIndex = path.get(i);
            Chunk chunk = args[tmpChunkIndex];

            String joshi = chunk.compoundFuncWord;            

            String verb = "NULL";
            if (chunk.hasVerb)
                verb = chunk.chead.REG;

            String regular;
            if (chunk.hasVerb)
                regular = chunk.chead.REG;
            else
                regular = chunk.chead.FORM;
            
            String direct;
            if (tmpChunkIndex > chunkIndex)
                direct = "0";
            else
                direct = "1";

            posPath += direct + chunk.chead.CPOS;
            verbPath += direct + chunk.sahenNoun + verb + joshi;
            regPath += direct + regular + joshi;
            auxPath += direct + chunk.particle;
            joshiPath += direct + joshi;

            chunkIndex = tmpChunkIndex;
        }
        
        return new String[]{posPath, verbPath, regPath, auxPath, joshiPath};
    }

}

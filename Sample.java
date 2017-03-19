

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
    final int nCases = 3;
    final public int size;
    final public Chunk[] chunks;
    final public Chunk[] prds;
    final public Chunk[] args;
    
    // oracleGraph 1D: nPrds, 2D: nCases
    public int[][] oracleGraph;
    public int[] oracleFeatIDs;
    public int[] featIDs;

    public Sample(Sentence sent) {
        chunks = setChunks(sent);
        prds = setPrds(sent);
        args = setArgs(sent);
        size = args.length;
        setOracleGraph();
    }
    
    final public int getPrdIndex(int prdGraphIndex) {
        return prds[prdGraphIndex].INDEX;
    }
    
    final public int getArgIndex(int argGraphIndex) {
        return args[argGraphIndex].INDEX;
    }
    
    private Chunk[] setChunks(Sentence sent) {
        Chunk[] chunks = new Chunk[sent.size()];
        for (int i=0; i<sent.size(); ++i)
            chunks[i] = sent.getChunk(i);
        return chunks;
    }
 
    private Chunk[] setPrds(Sentence sent) {
        Chunk[] prds = new Chunk[sent.prds.size()];
        for (int i=0; i<prds.length; ++i)
            prds[i] = sent.prds.get(i);
        return prds;
    }

    private Chunk[] setArgs(Sentence sent) {
        Chunk[] args = new Chunk[sent.size()+1];
        for (int i=0; i<args.length-1; ++i)
            args[i] = sent.getChunk(i);
        args[args.length-1] = setNullChunk(args.length-1);
        return args;
    }
    
    private Chunk setNullChunk(int chunkIndex) {
        Chunk chunk = new Chunk(chunkIndex, -1);
        chunk.setParams();
        return chunk;
    }

    final public void setOracleGraph() {
        oracleGraph = new int[prds.length][nCases];
        for (int prd_i=0; prd_i<prds.length; ++prd_i)
            for (int case_i=0; case_i<nCases; ++case_i)
                oracleGraph[prd_i][case_i] = getArgIndex(prds[prd_i], case_i);
    }
    
    private int getArgIndex(Chunk prd, int caseIndex) {
        int depArgIndex = prd.parsedDepCases[caseIndex];
        int zeroArgIndex = prd.parsedZeroCases[caseIndex];
        int nullArgIndex = args.length-1;

        if (depArgIndex > -1)
            return depArgIndex;
        else if (zeroArgIndex > -1)
            return zeroArgIndex;
        return nullArgIndex;
    }
    
    final public void setOracleFeatIDs(FeatureExtractor featExtractor) {
        oracleFeatIDs = featExtractor.getFeature(this);
    }

    final public static Chunk getNextChunk(Sample sample, Chunk chunk) {
        if (chunk.INDEX < sample.size)
            return sample.args[chunk.INDEX];
        return null;
    }


}

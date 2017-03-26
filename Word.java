/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.Serializable;

/**
 *
 * @author hiroki
 */

public class Word implements Serializable{
    final public static int NULL_ID = -1, EXO_ID = 1000;
    final public static int INTER_ARG_INDEX = -1;
    final public static int DEP = 0, INTRA_ZERO = 1, INTER_ZERO = 2;

    final public int INDEX, ID, CHUNK_INDEX, CHUNK_DEP_HEAD;
    final public String FORM, PRON, REG, CPOS, POS, TYPE, INFL;
    final public String[] PAS_INFO;
    final public boolean IS_PRD;

    final public int[] argIDs;
    final public int[][] argIndices;
    
    public Word(int index, int chunkIndex, int chunkHead, String[] info) {
        this.INDEX = index;
        this.FORM = info[0];
        this.PRON = info[1];
        this.REG = info[2];
        this.CPOS = info[3];
        this.POS = info[4];
        this.TYPE = info[5];
        this.INFL = info[6];
        this.PAS_INFO = info[7].split("/");

        this.CHUNK_INDEX = chunkIndex;
        this.CHUNK_DEP_HEAD = chunkHead;
        this.ID = setID();
        this.IS_PRD = isPrd();
        
        this.argIDs = setArgIDs();
        this.argIndices = genInitArgs();
    }
    
    final public static Word getNullWord() {
        String[] info = new String[]{"NULL", "NULL", "*", "NULL", "NULL", "*", "*", "_"};
        return new Word(-1, -1, -1, info);
    }

    private int setID() {
        for (int i=0; i<PAS_INFO.length; ++i) {
            String info = PAS_INFO[i];
            if (info.length() > 3 && "id=".equals(info.substring(0, 3)))
                return Integer.parseInt(info.substring(4, info.length()-1));
        }
        return NULL_ID;
    }
    
    private boolean isPrd() {
        for (int i=0; i<PAS_INFO.length; ++i)
            if ("type=\"pred\"".equals(PAS_INFO[i]))
                return true;
        return false;
    }
    
    private int[][] genInitArgs() {
        int[][] args = new int[Config.N_CASES][3];
        for (int i=0; i<args.length; ++i)
            for (int j=0; j<args[i].length; ++j)
                args[i][j] = -1;
        return args;
    }
    
    private int[] setArgIDs() {
        int[] argIDs = new int[]{-1, -1, -1};        
        for (int i=0; i<PAS_INFO.length; ++i) {
            String info = PAS_INFO[i];
            if (info.length() < 3)
                continue;

            if ("ga=".equals(info.substring(0, 3)))
                argIDs[0] = extractID(info.substring(4, info.length()-1));
            else if ("o=".equals(info.substring(0, 2)))
                argIDs[1] = extractID(info.substring(3, info.length()-1));
            else if ("ni=".equals(info.substring(0, 3)))
                argIDs[2] = extractID(info.substring(4, info.length()-1));
        }
        
        return argIDs;
    }

    private int extractID(String id) { 
        if (id.contains("exo"))
            return EXO_ID;             
        return Integer.parseInt(id);       
    }
    
    final public void setArgIndices(Sentence sent) {
        for (int caseLabel=0; caseLabel<argIDs.length; ++caseLabel) {
            int argIndex = getArgIndex(sent.chunks, argIDs[caseLabel]);
            int argType = getArgType(sent.chunks, argIndex);
            argIndices[caseLabel][argType] = argIndex;
        }
    }
    
    private int getArgIndex(Chunk[] chunks, int argID) {
        if (argID < 0)
            return INTER_ARG_INDEX;
        for (int chunkIndex=0; chunkIndex<chunks.length; ++chunkIndex) {
            Word[] words = chunks[chunkIndex].words;
            for (int wordIndex=0; wordIndex<words.length; ++wordIndex)
                if (words[wordIndex].ID == argID)
                    return chunkIndex;
        }
        return INTER_ARG_INDEX;
    }
    
    private int getArgType(Chunk[] chunks, int argIndex) {
        if (argIndex == INTER_ARG_INDEX)
            return INTER_ZERO;

        Chunk arg = chunks[argIndex];
        if (CHUNK_INDEX == arg.DEP_HEAD || CHUNK_DEP_HEAD == arg.INDEX)
            return DEP;
        return INTRA_ZERO;
    }

}

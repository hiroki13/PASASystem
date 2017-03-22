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

final public class Word implements Serializable{
    final static int NULL_ID = -1;
    final static int EXO_ID = 1000;

    final int INDEX, ID, CHUNK_INDEX, CHUNK_HEAD;
    final String FORM, PRON, REG, CPOS, POS, TYPE, INFL;
    final String[] PAS_INFO;
    final boolean IS_PRD;

    int ga = -1, o = -1, ni = -1;
    int zeroGa = -1, zeroO = -1, zeroNi = -1;
    int interGa = -1, interO = -1, interNi = -1;
    
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
        this.CHUNK_HEAD = chunkHead;
        this.ID = setID();
        this.IS_PRD = isPrd();
    }
    
    private boolean isPrd() {
        for (int i=0; i<PAS_INFO.length; ++i)
            if ("type=\"pred\"".equals(PAS_INFO[i]))
                return true;
        return false;
    }
    
    private int setID() {
        for (int i=0; i<PAS_INFO.length; ++i) {
            String info = PAS_INFO[i];
            if (info.length() > 3 && "id=".equals(info.substring(0, 3)))
                return Integer.parseInt(info.substring(4, info.length()-1));
        }
        return NULL_ID;
    }
    
    final public void setCaseArgIndex(Sentence sent) {
        ArrayList<Word> words = sent.words;
        int argID;        
        int argIndex;        
        int caseType;

        for (int i=0; i<PAS_INFO.length; ++i) {
            String info = PAS_INFO[i];

            if ("ga=".equals(info.substring(0, 3))) {
                argID = extractID(info.substring(4, info.length()-1));
                argIndex = getArgIndex(words, argID);
                caseType = getCaseType(words, argIndex);

                if (caseType == 0) ga = argIndex;
                else if (caseType == 1) zeroGa = argIndex;
                else interGa = argIndex;
            }
            else if ("o=".equals(info.substring(0, 2))) {
                argID = extractID(info.substring(3, info.length()-1));
                argIndex = getArgIndex(words, argID);
                caseType = getCaseType(words, argIndex);

                if (caseType == 0) o = argIndex;
                else if (caseType == 1) zeroO = argIndex;
                else interO = argIndex;
            }
            else if ("ni=".equals(info.substring(0, 3))) {
                argID = extractID(info.substring(4, info.length()-1));
                argIndex = getArgIndex(words, argID);
                caseType = getCaseType(words, argIndex);

                if (caseType == 0) ni = argIndex;
                else if (caseType == 1) zeroNi = argIndex;
                else interNi = argIndex;
            }
        }
    }
    
    private int extractID(String id) { 
        if (id.contains("exo"))
            return EXO_ID;             
        return Integer.parseInt(id);       
    }
    
    private int getArgIndex(ArrayList<Word> words, int argID) {
        for (int index=0; index<words.size(); ++index) {
            Word word = words.get(index);
            if (word.ID == argID)
                return index;
        }
        return EXO_ID;
    }
    
    private int getCaseType(ArrayList<Word> words, int argIndex) {
        if (argIndex == EXO_ID)
            return 2;

        Word arg = words.get(argIndex);
        if (CHUNK_INDEX == arg.CHUNK_HEAD || CHUNK_HEAD == arg.CHUNK_INDEX)
            return 0;
        return 1;
    }
    
}

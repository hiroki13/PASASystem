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
    final int INDEX, ID;
    final public String FORM, YOMI, R_FORM, CPOS, POS, INF_TYPE, INF_FORM;
    final public String[] PAS_INFO;
    final public boolean IS_PRD;
    final public Chunk CHUNK;

    int ga = -1, o = -1, ni = -1;
    int zeroGa = -1, zeroO = -1, zeroNi = -1;
    int interGa = -1, interO = -1, interNi = -1;
    
    public Word(int index, Chunk chunk, String[] info) {
        this.INDEX = index;
        this.FORM = info[0];
        this.YOMI = info[1];
        this.R_FORM = info[2];
        this.CPOS = info[3];
        this.POS = info[4];
        this.INF_TYPE = info[5];
        this.INF_FORM = info[6];
        this.PAS_INFO = info[7].split("/");

        this.ID = setId();
        this.IS_PRD = isPrd();
        this.CHUNK = chunk;
    }
    
    private boolean isPrd() {
        for (int i=0; i<PAS_INFO.length; ++i)
            if ("type=\"pred\"".equals(PAS_INFO[i]))
                return true;
        return false;
    }
    
    private int setId() {
        for (int i=0; i<PAS_INFO.length; ++i) {
            String info = PAS_INFO[i];
            if (info.length() > 3 && "id=".equals(info.substring(0, 3)))
                return Integer.parseInt(info.substring(4, info.length()-1));
        }
        return -2;
    }
    
    final public void setCaseArgIndex(Sentence sent) {
        ArrayList<Word> words = sent.words;
        int caseArgID;        
        int caseArgWordIndex;        
        int caseDepType;

        for (int i=0; i<PAS_INFO.length; ++i) {
            String info = PAS_INFO[i];

            if ("ga=".equals(info.substring(0, 3))) {
                caseArgID = getCaseArgId(info.substring(4, info.length()-1));
                caseArgWordIndex = getArgWordIndex(words, caseArgID);
                caseDepType = getCaseDepType(words, caseArgWordIndex);
                if (caseDepType == 0) ga = caseArgWordIndex;
                else if (caseDepType == 1) zeroGa = caseArgWordIndex;
                else interGa = caseArgWordIndex;
            }
            else if ("o=".equals(info.substring(0, 2))) {
                caseArgID = getCaseArgId(info.substring(3, info.length()-1));
                caseArgWordIndex = getArgWordIndex(words, caseArgID);
                caseDepType = getCaseDepType(words, caseArgWordIndex);
                if (caseDepType == 0) o = caseArgWordIndex;
                else if (caseDepType == 1) zeroO = caseArgWordIndex;
                else interO = caseArgWordIndex;
            }
            else if ("ni=".equals(info.substring(0, 3))) {
                caseArgID = getCaseArgId(info.substring(4, info.length()-1));
                caseArgWordIndex = getArgWordIndex(words, caseArgID);
                caseDepType = getCaseDepType(words, caseArgWordIndex);
                if (caseDepType == 0) ni = caseArgWordIndex;
                else if (caseDepType == 1) zeroNi = caseArgWordIndex;
                else interNi = caseArgWordIndex;
            }
        }
    }
    
    private int getCaseArgId(String id) { 
        if (id.contains("exo"))
            return 1000;             
        return Integer.parseInt(id);       
    }
    
    private int getArgWordIndex(ArrayList<Word> words, int argID) {
        for (int index=0; index<words.size(); ++index) {
            Word word = words.get(index);
            if (word.ID == argID)
                return index;
        }
        return 1000;
    }
    
    private int getCaseDepType(ArrayList<Word> words, int caseArgWordIndex) {
        if (caseArgWordIndex == 1000) return 2;
        Word caseArg = words.get(caseArgWordIndex);
        if (CHUNK.HEAD_INDEX == caseArg.CHUNK.INDEX || CHUNK.INDEX == caseArg.CHUNK.HEAD_INDEX)
            return 0;
        return 1;
    }
    
}

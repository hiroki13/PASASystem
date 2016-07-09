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

final public class Chunk implements Serializable{
    final int INDEX, DEP_HEAD_INDEX;
    final ArrayList<Word> words = new ArrayList();

    int ga = -1, o = -1, ni = -1, zeroGa = -1, zeroO = -1, zeroNi = -1;
    int[] parsedDepCases, parsedZeroCases;  // (ga, o, ni)
    
    Word chead, cfunc, prd;
    ArrayList sahenNoun = new ArrayList();
    boolean verb = false, sahenVerb = false;
    String caseAlter = "", compoundSahenNoun = "", compoundNoun = "", compoundJoshi = "";
    String aux, reg_form;

    boolean hasPrd = false;
    
    public Chunk(int index, int head) {
        this.INDEX = index;
        this.DEP_HEAD_INDEX = head;
    }
    
    final public void setParams(Sentence sent, int nCases) {                        
        setHead();                        
        setSahenWord();        
        setCaseAlterSuffix();        
        setCompoundWords();        
        setAux();        
        setRegForm();
        setPrds();
    }
    
    private void setPrds() {
        if (chead != null && chead.IS_PRD) {
            prd = chead;
            hasPrd = true;
        }
    }
    
    final public void setHead(){
        for (int i=0; i<this.words.size(); ++i) {
            Word word = this.words.get(i);

            if ("動詞".equals(word.CPOS)) this.verb = true;
            
            if (!"特殊".equals(word.CPOS) && !"助詞".equals(word.CPOS)
                && !"接尾辞".equals(word.CPOS) && !"助動詞".equals(word.CPOS))
                this.chead = word;
            else if (!"特殊".equals(word.CPOS))
                this.cfunc = word;
        }
        
        if (this.chead == null) this.chead = setNullHead();
    }
    
    private Word setNullHead() {
        String nullWordInfo = "NONE\tNONE\t*\tNONE\tNONE\t*\t*\t_";
        String[] split = nullWordInfo.split("\t");
        return new Word(-1, this, split);
    }

    final public void setSahenWord() {
        for (int i=0; i<this.words.size(); ++i) {
            Word token = (Word) this.words.get(i);

            if ("サ変名詞".equals(token.POS))
                this.sahenNoun.add(token);
            if ("サ変動詞".equals(token.INF_TYPE))
                this.sahenVerb = true;
        }
    }

    final public void setCaseAlterSuffix() {
        String alter1 = "0";
        String alter2 = "0";
        String alter3 = "0";

        for (int i=0; i<this.words.size(); ++i) {
            Word token = (Word) this.words.get(i);
            if (("れる".equals(token.R_FORM) || "れる".equals(token.FORM)
                || "られる".equals(token.R_FORM) || "られる".equals(token.FORM)
                || "せる".equals(token.R_FORM) || "せる".equals(token.FORM)) &&
                "接尾辞".equals(token.CPOS))                
                alter1 = "1";
            if (("できる".equals(token.R_FORM) || "できる".equals(token.FORM)
                || "出来る".equals(token.R_FORM) || "出来る".equals(token.FORM)) &&
                this.sahenNoun.size() > 0)
                alter2 = "1";
            if (token.INF_FORM.startsWith("デアル列"))
                alter3 = "1";
        }

        this.caseAlter = alter1+alter2+alter3;
    }
    
    final public void setCompoundWords() {
        Word chead = this.chead;
        if (chead != null) {
            if (this.sahenVerb) {
                for(int j=0; j<this.sahenNoun.size(); ++j) {
                    this.compoundSahenNoun += ((Word) this.sahenNoun.get(j)).FORM;
                }
            }
            
            int head = 100;
            for (int j=0; j<this.words.size(); ++j) {
                String t = ((Word) this.words.get(j)).FORM;
                this.compoundNoun += t;
                if (t.equals(chead.FORM)) {
                    head = j+1;
                    break;
                }
            }
            for (int j=head; j<this.words.size(); ++j) {
                String t = ((Word) this.words.get(j)).FORM;
                this.compoundJoshi += t;
            }
        }
    }
    
    final public void setAux() {
        if (this.cfunc == null) this.aux = "";
	else this.aux = this.cfunc.FORM;
    }
    
    final public void setRegForm() {
        String rform = this.chead.R_FORM;

        if (this.sahenVerb) this.reg_form = this.compoundSahenNoun;
        else if (!"*".equals(rform)) this.reg_form = rform;
        else this.reg_form = this.chead.FORM;
    }

}

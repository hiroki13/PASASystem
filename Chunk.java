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
    final int INDEX, HEAD_INDEX;
    final ArrayList<Word> words = new ArrayList();

    int ga = -1, o = -1, ni = -1, zeroGa = -1, zeroO = -1, zeroNi = -1;
    int[] parsedDepCases, parsedZeroCases;  // (ga, o, ni)
    
    Word chead;
    Word cfunc;
    Word prd;
    ArrayList<Word> sahenNoun = new ArrayList();

    boolean isVerb = false;
    boolean isSahenVerb = false;
    boolean hasPrd = false;
    
    String caseAlterSuffix = "";
    String compoundSahenNoun = "";
    String compoundNoun = "";
    String compoundFuncWord = "";
    String particle;
    String regform;
    
    public Chunk(int index, int headIndex) {
        this.INDEX = index;
        this.HEAD_INDEX = headIndex;
    }
    
    final public void add(Word word) {
        words.add(word);
    }
    
    final public void setParams() {
        setHead();
        setPrds();

        setSahenWord();        
        setCaseAlterSuffix();        
        setCompoundWords();        
        setParticle();        
        setRegForm();
    }
    
    final public void setHead(){
        for (int i=0; i<this.words.size(); ++i) {
            Word word = this.words.get(i);

            if ("動詞".equals(word.CPOS))
                this.isVerb = true;
            
            if (!"特殊".equals(word.CPOS)
                    && !"助詞".equals(word.CPOS)
                    && !"接尾辞".equals(word.CPOS)
                    && !"助動詞".equals(word.CPOS))
                this.chead = word;
            else if (!"特殊".equals(word.CPOS))
                this.cfunc = word;
        }
        
        if (this.chead == null)
            this.chead = setNullWord();
    }
    
    private void setPrds() {
        if (chead != null && chead.IS_PRD) {
            prd = chead;
            hasPrd = true;
        }
        if (cfunc != null && cfunc.IS_PRD) {
            prd = cfunc;
            hasPrd = true;
        }
    }
    
    private Word setNullWord() {
        String[] tokenInfo = new String[]{"NULL", "NULL", "*", "NULL",
                                          "NULL", "*", "*", "_"};
        return new Word(-1, this, tokenInfo);
    }

    final public void setSahenWord() {
        for (int i=0; i<this.words.size(); ++i) {
            Word token = this.words.get(i);

            if ("サ変名詞".equals(token.POS))
                this.sahenNoun.add(token);
            if ("サ変動詞".equals(token.INF_TYPE))
                this.isSahenVerb = true;
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

        this.caseAlterSuffix = alter1 + alter2 + alter3;
    }
    
    final public void setCompoundWords() {
        Word chead = this.chead;
        if (chead != null) {
            if (this.isSahenVerb) {
                for(int i=0; i<this.sahenNoun.size(); ++i) {
                    this.compoundSahenNoun += this.sahenNoun.get(i).FORM;
                }
            }
            
            int head = 100;
            for (int i=0; i<this.words.size(); ++i) {
                String form = this.words.get(i).FORM;
                this.compoundNoun += form;
                if (form.equals(chead.FORM)) {
                    head = i+1;
                    break;
                }
            }

            for (int i=head; i<this.words.size(); ++i) {
                String form = this.words.get(i).FORM;
                this.compoundFuncWord += form;
            }
        }
    }
    
    final public void setParticle() {
        if (this.cfunc == null)
            this.particle = "";
	else
            this.particle = this.cfunc.FORM;
    }
    
    final public void setRegForm() {
        String rform = this.chead.R_FORM;

        if (this.isSahenVerb)
            this.regform = this.compoundSahenNoun;
        else if (!"*".equals(rform))
            this.regform = rform;
        else
            this.regform = this.chead.FORM;
    }

}

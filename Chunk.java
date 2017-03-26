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
    final public int INDEX, DEP_HEAD;
    final public Word[] words;

    final public Word chead, cfunc, prd;
    final public boolean hasPrd, hasVerb, hasSahenVerb;    
    
    final public String sahenNoun, compoundContWord, compoundFuncWord;
    final public String regform, particle, voiceSuffix;
    
    public Chunk(int index, int dep_head, ArrayList<Word> words) {
        this.INDEX = index;
        this.DEP_HEAD = dep_head;
        this.words = setWords(words);

        this.chead = setHead();
        this.cfunc = setFunc();
        this.prd = setPrd();
        
        this.hasPrd = setHasPrd();
        this.hasVerb = setHasVerb();
        this.hasSahenVerb = setHasSahenVerb();

        this.sahenNoun = setSahenNoun();
        this.compoundContWord = setCompoundContWord();
        this.compoundFuncWord = setCompoundFuncWord();

        this.regform = setRegForm();
        this.particle = setParticle();
        this.voiceSuffix = setVoiceSuffix();
    }
    
    final public static Chunk getNullChunk(int chunkIndex) {
        return new Chunk(chunkIndex, -1, new ArrayList());        
    }

    private Word[] setWords(ArrayList<Word> tmpWords) {
        Word[] words = new Word[tmpWords.size()];
        for (int i=0; i<tmpWords.size(); ++i)
            words[i] = tmpWords.get(i);
        return words;
    }

    private Word setHead() {
        Word head = null;
        for (int i=0; i<this.words.length; ++i) {
            Word word = this.words[i];

            if (!"特殊".equals(word.CPOS)
                    && !"助詞".equals(word.CPOS)
                    && !"接尾辞".equals(word.CPOS)
                    && !"助動詞".equals(word.CPOS))
                head = word;
        }
        
        if (head == null)
            return Word.getNullWord();
        return head;
    }
    
    private Word setFunc() {
        Word func = null;
        for (int i=0; i<this.words.length; ++i) {
            Word word = this.words[i];

            if (!"特殊".equals(word.CPOS) && chead != word)
                func = word;
        }
        
        if (func == null)
            return Word.getNullWord();
        return func;
    }
    
    private Word setPrd() {
        Word prd = null;
        for (int i=0; i<words.length; ++i) {
            Word word = words[i];
            if (word.IS_PRD)
                prd = word;
        }
        return prd;
    }
    
    private boolean setHasPrd() {
        return prd != null;
    }
    
    private boolean setHasVerb(){
        for (int i=0; i<this.words.length; ++i) {
            Word word = this.words[i];
            if ("動詞".equals(word.CPOS))
                return true;
        }
        return false;
    }
    
    private boolean setHasSahenVerb() {
        for (int i=0; i<this.words.length; ++i) {
            Word word = this.words[i];
            if ("サ変動詞".equals(word.TYPE))
                return true;
        }
        return false;
    }

    private String setSahenNoun() {
        String sahenNoun = "";
        for (int i=0; i<this.words.length; ++i) {
            Word word = this.words[i];
            if ("サ変名詞".equals(word.POS))
                sahenNoun += word.FORM;
        }
        return sahenNoun;
    }
    
    private String setCompoundContWord() {
        String compoundContWord = "";
        if (chead.INDEX < 0)
            return compoundContWord;
        for (int i=0; i<this.words.length; ++i) {
            Word word = this.words[i];
            compoundContWord += word.FORM;
            if (word == chead)
                break;
        }
        return compoundContWord;
    }

    private String setCompoundFuncWord() {
        String compoundFuncWord = "";
        if (chead.INDEX < 0)
            for (int i=0; i<this.words.length; ++i)
                compoundFuncWord += this.words[i].FORM;
        else
            for (int i=0; i<this.words.length; ++i) {
                Word word = this.words[i];
                if (word.INDEX <= chead.INDEX)
                    continue;
                compoundFuncWord += word.FORM;
            }
        return compoundFuncWord;
    }

    private String setRegForm() {
        if (this.hasSahenVerb)
            return this.sahenNoun;
        String rform = this.chead.REG;
        if (!"*".equals(rform))
            return rform;
        return this.chead.FORM;
    }
    
    private String setParticle() {
        return this.cfunc.FORM;
    }
    
    private String setVoiceSuffix() {
        String voice1 = "0";
        String voice2 = "0";
        String voice3 = "0";

        for (int i=0; i<this.words.length; ++i) {
            Word token = this.words[i];
            if (("れる".equals(token.REG) || "れる".equals(token.FORM)
                || "られる".equals(token.REG) || "られる".equals(token.FORM)
                || "せる".equals(token.REG) || "せる".equals(token.FORM)) &&
                "接尾辞".equals(token.CPOS))                
                voice1 = "1";
            if (("できる".equals(token.REG) || "できる".equals(token.FORM)
                || "出来る".equals(token.REG) || "出来る".equals(token.FORM)) &&
                this.sahenNoun.length() > 0)
                voice2 = "1";
            if (token.INFL.startsWith("デアル列"))
                voice3 = "1";
        }

        return voice1 + voice2 + voice3;
    }
    
    final public int size() {
        return words.length;
    }
    
    final public Word getWord(int index) {
        return words[index];
    }
    
}

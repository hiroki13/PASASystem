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

    int[] ga, o, ni, zero_ga, zero_o, zero_ni;
    int[][] parsedCases, parsedZeroCases;  // (ga, o, ni)
    
    Word chead, cfunc;
    ArrayList sahen_noun = new ArrayList<>();
    boolean verb = false, sahen_verb = false;
    String case_alter = "";
    String compound_sahen_noun = "";
    String compound_noun = "";
    String compound_joshi = "";
    String aux, reg_form;

    boolean pred = false;        
    
    public Chunk(String[] input_info){
        this.INDEX = Integer.parseInt(input_info[1]);
        this.DEP_HEAD_INDEX = setPas(input_info[2]);

        this.ga = setAddPas(input_info[3]);
        this.o = setAddPas(input_info[4]);
        this.ni = setAddPas(input_info[5]);

        this.zero_ga = setAddPas(input_info[6]);
        this.zero_o = setAddPas(input_info[7]);
        this.zero_ni = setAddPas(input_info[8]);
        
        if ("PRED".equals(input_info[12])) this.pred = true;
    }
    
    public Chunk(int index, int head) {
        this.INDEX = index;
        this.DEP_HEAD_INDEX = head;
    }
    
    final public void setParams() {                        
        setHead();                        
        setSahenNoun();        
        setCaseAlterSuffix();        
        setCompounds();        
        setAux();        
        setRegForm();        
    }

    final public void setParsedCase(int[] caseLabels) {
        this.parsedCases = new int[caseLabels.length][];
        this.parsedZeroCases = new int[caseLabels.length][];

        for (int i=0; i<caseLabels.length; ++i) {
            int case_label = caseLabels[i];
            int[] caseDep;
            int[] caseZero;

            if (case_label == 0) {
                caseDep = this.ga;
                caseZero = this.zero_ga;
            }
            else if (case_label == 1) {
                caseDep = this.o;
                caseZero = this.zero_o;
            }
            else {
                caseDep = this.ni;
                caseZero = this.zero_ni;
            }

            this.parsedCases[i] = caseDep;
            this.parsedZeroCases[i] = caseZero;
        }
    }

    final public void setHead(){
        for (int i=0; i<this.words.size(); ++i) {
            Word token = (Word) this.words.get(i);

            if ("動詞".equals(token.CPOS)) this.verb = true;
            
            if (!"特殊".equals(token.CPOS) && !"助詞".equals(token.CPOS)
                && !"接尾辞".equals(token.CPOS) && !"助動詞".equals(token.CPOS)) {
                this.chead = token;
            }
            else if (!"特殊".equals(token.CPOS)) {
                this.cfunc = token;
            }
        }
        
        if (this.chead == null)
            this.chead = setNoneHead();
    }
    
    private Word setNoneHead() {
        final String none_token_info = "NONE\tNONE\t*\tNONE\tNONE\t*\t*\t_";
        String[] split = none_token_info.split("\t");
        Word token = new Word(-1, this, split);
        return token;
    }

    final public void setSahenNoun() {
        for (int i=0; i<this.words.size(); ++i) {
            Word token = (Word) this.words.get(i);

            if ("サ変名詞".equals(token.POS))
                this.sahen_noun.add(token);
            if ("サ変動詞".equals(token.INF_TYPE))
                this.sahen_verb = true;
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
                this.sahen_noun.size() > 0)
                alter2 = "1";
            if (token.INF_FORM.startsWith("デアル列"))
                alter3 = "1";
        }

        this.case_alter = alter1+alter2+alter3;
    }
    
    final public void setCompounds() {
        Word chead = this.chead;
        if (chead != null) {
            if (this.sahen_verb) {
                for(int j=0; j<this.sahen_noun.size(); ++j) {
                    this.compound_sahen_noun += ((Word) this.sahen_noun.get(j)).FORM;
                }
            }
            
            int head = 100;
            for (int j=0; j<this.words.size(); ++j) {
                String t = ((Word) this.words.get(j)).FORM;
                this.compound_noun += t;
                if (t.equals(chead.FORM)) {
                    head = j+1;
                    break;
                }
            }
            for (int j=head; j<this.words.size(); ++j) {
                String t = ((Word) this.words.get(j)).FORM;
                this.compound_joshi += t;
            }
        }
    }
    
    final public void setAux() {
        if (this.cfunc == null) this.aux = "";
	else this.aux = this.cfunc.FORM;
    }
    
    final public void setRegForm() {
        String rform = this.chead.R_FORM;

        if (this.sahen_verb) this.reg_form = this.compound_sahen_noun;
        else if (!"*".equals(rform)) this.reg_form = rform;
        else this.reg_form = this.chead.FORM;
    }

    private int[] setAddPas(String pas) {
        String[] tmp = pas.split("/");
        int[] pas_info = new int[tmp.length];
        for (int i=0; i<tmp.length; ++i) {
            pas_info[i] = setPas(tmp[i]);
        }
        return pas_info;
    }

    private int setPas(String pas) {
        if ("*".equals(pas)) return -1;
        return Integer.parseInt(pas);
    }
        
}

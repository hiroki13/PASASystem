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
    final int index;
    final int head;

    int[] ga;
    final int[] o;
    final int[] ni;
    int[] zero_ga;
    final int[] zero_o;
    final int[] zero_ni;

    int[][] parsed_cases;  // (ga, o, ni)
    int[][] parsed_zero_cases;  // (zero_ga, zero_o, zero_ni)
    
    Token chead;
    Token cfunc;
    ArrayList sahen_noun = new ArrayList<>();
    boolean verb = false;
    boolean sahen_verb = false;
    String case_alter = "";
    String compound_sahen_noun = "";
    String compound_noun = "";
    String compound_joshi = "";
    String aux;
    String reg_form;

    boolean pred = false;
        
    ArrayList tokens;
    
    public Chunk(String[] input_info){
        this.index = Integer.parseInt(input_info[1]);
        this.head = setPas(input_info[2]);

        this.ga = setAddPas(input_info[3]);
        this.o = setAddPas(input_info[4]);
        this.ni = setAddPas(input_info[5]);

        this.zero_ga = setAddPas(input_info[6]);
        this.zero_o = setAddPas(input_info[7]);
        this.zero_ni = setAddPas(input_info[8]);
        
        if ("PRED".equals(input_info[12])) this.pred = true;
        this.tokens = new ArrayList();
    }

    final public void setParsedCase(int case_label) {
        if (case_label == 1) {
            ga = o;
            zero_ga = zero_o;
        }
        else if (case_label == 2) {
            ga = ni;
            zero_ga = zero_ni;
        }
    }

    final public void setParsedCase(int[] case_labels) {
        this.parsed_cases = new int[case_labels.length][];
        this.parsed_zero_cases = new int[case_labels.length][];

        for (int i=0; i<case_labels.length; ++i) {
            int case_label = case_labels[i];
            int[] case_dep;
            int[] case_zero;

            if (case_label == 0) {
                case_dep = this.ga;
                case_zero = this.zero_ga;
            }
            else if (case_label == 1) {
                case_dep = this.o;
                case_zero = this.zero_o;
            }
            else {
                case_dep = this.ni;
                case_zero = this.zero_ni;
            }

            this.parsed_cases[i] = case_dep;
            this.parsed_zero_cases[i] = case_zero;
        }
    }

    final public void setHead(){
        for (int i=0; i<this.tokens.size(); ++i) {
            Token token = (Token) this.tokens.get(i);

            if ("動詞".equals(token.cpos)) this.verb = true;
            
            if (!"特殊".equals(token.cpos) && !"助詞".equals(token.cpos)
                && !"接尾辞".equals(token.cpos) && !"助動詞".equals(token.cpos)) {
                this.chead = token;
            }
            else if (!"特殊".equals(token.cpos)) {
                this.cfunc = token;
            }
        }
        
        if (this.chead == null)
            this.chead = setNoneHead();
    }
    
    final private Token setNoneHead() {
        final String none_token_info = "NONE\tNONE\t*\tNONE\tNONE\t*\t*\t_";
        String[] split = none_token_info.split("\t");
        Token token = new Token(split[0], split[1], split[2], split[3],
                                split[4], split[5], split[6], split[7]);
        return token;
    }

    final public void setSahenNoun() {
        for (int i=0; i<this.tokens.size(); ++i) {
            Token token = (Token) this.tokens.get(i);

            if ("サ変名詞".equals(token.pos))
                this.sahen_noun.add(token);
            if ("サ変動詞".equals(token.inf_type))
                this.sahen_verb = true;
        }
    }

    final public void setCaseAlterSuffix() {
        String alter1 = "0";
        String alter2 = "0";
        String alter3 = "0";

        for (int i=0; i<this.tokens.size(); ++i) {
            Token token = (Token) this.tokens.get(i);
            if (("れる".equals(token.r_form) || "れる".equals(token.form)
                || "られる".equals(token.r_form) || "られる".equals(token.form)
                || "せる".equals(token.r_form) || "せる".equals(token.form)) &&
                "接尾辞".equals(token.cpos))                
                alter1 = "1";
            if (("できる".equals(token.r_form) || "できる".equals(token.form)
                || "出来る".equals(token.r_form) || "出来る".equals(token.form)) &&
                this.sahen_noun.size() > 0)
                alter2 = "1";
            if (token.inf_form.startsWith("デアル列"))
                alter3 = "1";
        }

        this.case_alter = alter1+alter2+alter3;
    }
    
    final public void setCompounds() {
        Token chead = this.chead;
        if (chead != null) {
            if (this.sahen_verb) {
                for(int j=0; j<this.sahen_noun.size(); ++j) {
                    this.compound_sahen_noun += ((Token) this.sahen_noun.get(j)).form;
                }
            }
            
            int head = 100;
            for (int j=0; j<this.tokens.size(); ++j) {
                String t = ((Token) this.tokens.get(j)).form;
                this.compound_noun += t;
                if (t.equals(chead.form)) {
                    head = j+1;
                    break;
                }
            }
            for (int j=head; j<this.tokens.size(); ++j) {
                String t = ((Token) this.tokens.get(j)).form;
                this.compound_joshi += t;
            }
        }
    }
    
    final public void setAux() {
        if (this.cfunc == null) this.aux = "";
	else this.aux = this.cfunc.form;
    }
    
    final public void setRegForm() {
        String rform = this.chead.r_form;

        if (this.sahen_verb) this.reg_form = this.compound_sahen_noun;
        else if (!"*".equals(rform)) this.reg_form = rform;
        else this.reg_form = this.chead.form;
    }

    final private int[] setAddPas(String pas) {
        String[] tmp = pas.split("/");
        int[] pas_info = new int[tmp.length];
        for (int i=0; i<tmp.length; ++i) {
            pas_info[i] = setPas(tmp[i]);
        }
        return pas_info;
    }

    final private int setPas(String pas) {
        if ("*".equals(pas)) return -1;
        return Integer.parseInt(pas);
    }
        
}

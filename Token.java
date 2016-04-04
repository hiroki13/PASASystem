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

final public class Token implements Serializable{
    final public String form;
    final public String yomi;
    final public String r_form;
    final public String cpos;
    final public String pos;
    final public String inf_type;
    final public String inf_form;
    final public String pas;
    
    public Token(String form, String yomi, String r_form, String cpos,
                String pos, String inf_type, String inf_form, String pas) {
        this.form = form;
        this.yomi = yomi;
        this.r_form = r_form;
        this.cpos = cpos;
        this.pos = pos;
        this.inf_type = inf_type;
        this.inf_form = inf_form;
        this.pas = pas;
    }
    
}

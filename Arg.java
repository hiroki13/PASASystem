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

final public class Arg implements Serializable{
    final String form;
    final int freq;
    
    Arg(String name, int freq) {
        this.form = getForm(name);
        this.freq = freq;
    }
    
    final private String getForm(String name) {
        String[] split_word = name.split("＋");
        String[] split_yomi = split_word[0].split("/");
        return split_yomi[0];
    }
}

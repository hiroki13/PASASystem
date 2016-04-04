/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author hiroki
 */

final public class Reader {
    public int sent_i;
    public int max_sent_length;
    
    public Reader() {
        max_sent_length = 0;
    }
    
    final public List<Sentence> read(final String fn, final int n_cases,
                                      final int[] case_label) throws Exception{
        final String delimiter = "\t";
        sent_i = 0;
        
        String line;
        Chunk chunk;
        Chunk NULL = setNull();
        List<Sentence> sentenceList = new ArrayList<>();
        Sentence sentence = new Sentence(sent_i++, n_cases);
        
        try(BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(fn)))){

            while((line=br.readLine())!=null){

                if("EOS".equals(line)){
                    // Sentence info
                    boolean cycle = false;  // cycle=dependency cycle
                    sentence.chunks.add(NULL);  // NULL node set
                    
                    for (int j=0; j<sentence.size()-1; ++j) {
                        chunk = (Chunk) sentence.chunks.get(j);

                        // check if the dependency tree has a cycle
                        if (chunk.index == chunk.head) {
                            cycle = true;
                            break;
                        }

                        // set chunk params
                        chunk.setHead();
                        chunk.setSahenNoun();
                        chunk.setCaseAlterSuffix();
                        chunk.setCompounds();
                        chunk.setAux();
                        chunk.setRegForm();

                        sentence.chunks.set(j, chunk);
                        
                    }
                    
                    if (!cycle) {
                        // Set sentence params
                        sentence.setParsedCases(case_label);
                        sentence.setArgCandidates();
                        sentence.setPrds();
                        sentence.setOracleArgs();
                        sentence.setDeps();
                        sentence.setTotalNumCaseArgs();
                        
                        if (sentence.size() > max_sent_length) {
                            max_sent_length = sentence.size();
                        }
                        
                        sentenceList.add(sentence);
                    }
                    
                    sentence = new Sentence(sent_i++, n_cases);
                }
                else if (line.startsWith("*")) {
                    // Chunk info
                    String[] chunk_info = line.split(" ");
                    sentence.add(new Chunk(chunk_info));
                }
                else if (!line.startsWith("#")) {
                    // Token info
                    String[] token_info = line.split(delimiter);

                    Token token = new Token(token_info[0], token_info[1],
                                            token_info[2], token_info[3],
                                            token_info[4], token_info[5],
                                            token_info[6], token_info[7]);

                    int index = sentence.size()-1;
                    chunk = (Chunk) sentence.chunks.get(index);
                    chunk.tokens.add(token);
                    sentence.chunks.set(index, chunk);
                }
                else {
                    String[] split = line.split(" ");
                    sentence.ntc_id = Integer.parseInt(split[1]);
                }
            }
        }
        return sentenceList;
    }
    
    final private Chunk setNull() {
        final String null_chunk_info = "* -2 -3 * * * * * * * * * NONE";
        final String null_token_info = "NULL\tNULL\t*\tNULL\tNULL\t*\t*\t_";
        Chunk chunk = new Chunk(null_chunk_info.split(" "));
        String[] split = null_token_info.split("\t");
        Token token = new Token(split[0], split[1], split[2], split[3],
                                split[4], split[5], split[6], split[7]);
        chunk.tokens.add(token);
        chunk.setHead();
        chunk.setCaseAlterSuffix();
        chunk.setAux();
        chunk.setRegForm();
        
        return chunk;
    }
    
    final private int[] convertString2Int(String[] hoge) {
        final int[] array = new int[hoge.length];
        for (int i=0; i<hoge.length; ++i) {
            array[i] = Integer.parseInt(hoge[i]);
        }
        return array;
    }
}

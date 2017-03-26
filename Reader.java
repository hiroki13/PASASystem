/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 *
 * @author hiroki
 */

public class Reader {
    final private String SPACE = " ";
    final private String EOS = "EOS";
    
    public Reader() {}
    
    final public ArrayList<Sentence> read(String fn) throws Exception{
        ArrayList<Sentence> corpus = new ArrayList();

        if (fn == null)
            return corpus;

        String line;
        ArrayList<Word> words = new ArrayList();
        int sentIndex = 0, wordIndex = 0, chunkIndex = -1, chunkHead = -1;

//        Sentence sent = new Sentence(sentIndex++);
        ArrayList<Chunk> chunks = new ArrayList();
        BufferedReader br = getFile(fn);

        while((line=br.readLine()) != null) {
            if(EOS.equals(line)){                    
                chunks.add(new Chunk(chunkIndex, chunkHead, words));
                corpus.add(new Sentence(sentIndex++, chunks));
                        
                if (corpus.size() == 1000) break;

                chunks = new ArrayList();
                wordIndex = 0;
                words = new ArrayList();
            }
            else if (line.startsWith("*")) {
                if (words.size() > 0)
                    chunks.add(new Chunk(chunkIndex, chunkHead, words));
                words = new ArrayList();
                String[] depInfo = line.split(SPACE);
                chunkIndex = Integer.parseInt(depInfo[1]);
                chunkHead = Integer.parseInt(depInfo[2].substring(0, depInfo[2].length()-1));
            }
            else if (line.startsWith("#")) {}
            else {
                Word word = new Word(wordIndex++, chunkIndex, chunkHead, line.split(SPACE));
                words.add(word);
            }
        }
        
        return corpus;
    }
    
    private BufferedReader getFile(String fn) throws FileNotFoundException {
        return new BufferedReader(new InputStreamReader(new FileInputStream(fn)));
    }
    
}

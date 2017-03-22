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

final public class Reader {
    final String TAB = "\t";
    final String SPACE = " ";
    final String EOS = "EOS";
    final int N_CASES;
    
    public Reader(int nCases) {
        N_CASES = nCases;
    }
    
    final public ArrayList<Sentence> read(String fn) throws Exception{
        ArrayList<Sentence> corpus = new ArrayList();

        if (fn == null)
            return corpus;

        String line;
        int docIndex=0, sentIndex = 0, wordIndex = 0;

        Sentence sent = new ChunkSentence(sentIndex++, N_CASES);
        BufferedReader br = getFile(fn);

        while((line=br.readLine()) != null) {
            if(EOS.equals(line)){                    
                sent.setParams();
                corpus.add(sent);
                        
                if (corpus.size() == 100) break;

                sent = new ChunkSentence(sentIndex++, N_CASES);                        
                wordIndex = 0;
            }
            else if (line.startsWith("*")) {
                String[] depInfo = line.split(SPACE);
                int chunkIndex = Integer.parseInt(depInfo[1]);
                int chunkHeadIndex = Integer.parseInt(depInfo[2].substring(0, depInfo[2].length()-1));
                sent.chunks.add(new Chunk(chunkIndex, chunkHeadIndex));
            }
            else if (line.startsWith("#"))
                sent.ntcId = docIndex++;
            else {
                Chunk chunk = sent.chunks.get(sent.chunks.size()-1);
                Word word = new Word(wordIndex++, chunk, line.split(SPACE));
                sent.words.add(word);
                chunk.words.add(word);
            }
        }
        
        return corpus;
    }
    
    private BufferedReader getFile(String fn) throws FileNotFoundException {
        return new BufferedReader(new InputStreamReader(new FileInputStream(fn)));
    }
    
}

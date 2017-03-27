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

public class Sentence implements Serializable{

    final public int INDEX;
    final public Word[] words;
    final public Chunk[] chunks, prds;
    final public boolean hasPrds;
    
    public Sentence(int index, ArrayList<Chunk> chunks) {        
        this.INDEX = index;
        this.chunks = setChunks(chunks);
        this.words = setWords();
        this.prds = setPrds();        
        this.hasPrds = setHasPrds();
        setOracleArgIndex();
    }
    
    private Chunk[] setChunks(ArrayList<Chunk> tmpChunks) {
        Chunk[] chunks = new Chunk[tmpChunks.size()];
        for (int i=0; i<tmpChunks.size(); ++i)
            chunks[i] = tmpChunks.get(i);
        return chunks;
    }

    private Word[] setWords() {
        int nWords = 0;
        for (int i=0; i<chunks.length; ++i)
            nWords += chunks[i].size();

        Word[] words = new Word[nWords];
        int wordIndex = 0;
        for (int i=0; i<chunks.length; ++i) {
            Word[] tmpWords = chunks[i].words;
            for (int j=0; j<tmpWords.length; ++j)
                words[wordIndex++] = tmpWords[j];
        }
        return words;
    }
    
    private Chunk[] setPrds() {
        int nPrds = 0;
        for (int i=0; i<chunks.length; ++i)
            if (chunks[i].hasPrd)
                nPrds += 1;
        
        Chunk[] prds = new Chunk[nPrds];
        int prdIndex = 0;
        for (int i=0; i<chunks.length; ++i)
            if (chunks[i].hasPrd)
                prds[prdIndex++] = chunks[i];
        return prds;
        
    }

    private boolean setHasPrds() {
        return prds.length > 0;
    }
    
    private void setOracleArgIndex() {
        for (int i=0; i<words.length; ++i) {
            Word word = words[i];
            if (word.IS_PRD)
                word.setArgIndices(this);
        }
    }
    
    final public Chunk getChunk(int index) {
        return chunks[index];
    }
    
    final public Word getWord(int index) {
        return words[index];
    }
    
    final public int sizeChunks() {
        return chunks.length;
    }

    final public int sizeWords() {
        return words.length;
    }

}

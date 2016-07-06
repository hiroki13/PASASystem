/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.BufferedReader;
import java.io.FileInputStream;
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
    final Chunk NULL_CHUNK = setNullChunk();
    final Word NULL_WORD = NULL_CHUNK.words.get(0);
    public int maxSentLen;
    
    public Reader() {
        maxSentLen = 0;
    }
    
    final public ArrayList<Sentence> read(String fn, int nCases, int[] caseLabels) throws Exception{
        String line;
        int docIndex=0, sentIndex = 0, wordIndex = 0;
        ArrayList<Sentence> corpus = new ArrayList<>();

        Sentence sent = new ChunkSentence(sentIndex++, nCases);
        
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fn)))){
            while((line=br.readLine()) != null){
                if(EOS.equals(line)){
                    sent.chunks.add(NULL_CHUNK);  // Set the NULL node
                    
                    if (!sent.hasDepCicle()) {
                        sent.setParams(caseLabels);
                        corpus.add(sent);
                        
                        if (sent.size() > maxSentLen)
                            maxSentLen = sent.size();                        
                    }
                    
                    sent = new ChunkSentence(sentIndex++, nCases);
                    wordIndex = 0;
                }
                else if (line.startsWith("*"))
                    sent.add(new Chunk(line.split(SPACE)));
                else if (line.startsWith("#"))
                    sent.ntcId = docIndex++;
                else {
                    Chunk chunk = sent.chunks.get(sent.chunks.size()-1);
                    Word word = new Word(wordIndex++, chunk, line.split(TAB));
                    sent.words.add(word);
                    chunk.words.add(word);
                }
            }
        }
        
        return corpus;
    }
    
    final public ArrayList<Sentence> readWord(String fn, int nCases, int[] caseLabels) throws Exception{
        String line;
        int docIndex=0, sentIndex = 0, wordIndex = 0;
        ArrayList<Sentence> corpus = new ArrayList<>();

        Sentence sent = new WordSentence(sentIndex++, nCases);
        
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fn)))){
            while((line=br.readLine()) != null){
                if(EOS.equals(line)) {
                    sent.chunks.add(NULL_CHUNK);
                    sent.words.add(NULL_WORD);
                    sent.setParams();
                    
                    if (!sent.hasDepCicle()) {
                        corpus.add(sent);
                        
                        if (sent.size() > maxSentLen)
                            maxSentLen = sent.size();                        
                        
                        if (corpus.size() == 100) break;
                    }
                    
                    sent = new WordSentence(sentIndex++, nCases);
                    wordIndex = 0;
                }
                else if (line.startsWith("*")) {
                    String[] depInfo = line.split(SPACE);
                    int chunkIndex = Integer.parseInt(depInfo[1]);
                    int chunkDepHead = Integer.parseInt(depInfo[2].substring(0, depInfo[2].length()-1));
                    sent.chunks.add(new Chunk(chunkIndex, chunkDepHead));
                }
                else if (line.startsWith("#")) {
                    sent.ntcId = docIndex++;
                }
                else {
                    Chunk chunk = sent.chunks.get(sent.chunks.size()-1);
                    Word word = new Word(wordIndex++, chunk, line.split(SPACE));
                    sent.words.add(word);
                    chunk.words.add(word);
                }
            }
        }
        
        return corpus;
    }
    
    private Chunk setNullChunk() {
//        final String chunkInfo = "* -2 -3 * * * * * * * * * NONE";
        final String chunkInfo = "* -2 -3D";
        final String tokenInfo = "NULL\tNULL\t*\tNULL\tNULL\t*\t*\t_";
        String[] depInfo = chunkInfo.split(SPACE);        
        int chunkIndex = Integer.parseInt(depInfo[1]);        
        int chunkDepHead = Integer.parseInt(depInfo[2].substring(0, depInfo[2].length()-1));

        Chunk chunk = new Chunk(chunkIndex, chunkDepHead);
        chunk.words.add(new Word(-1, chunk, tokenInfo.split("\t")));
        chunk.setHead();
        chunk.setCaseAlterSuffix();
        chunk.setAux();
        chunk.setRegForm();        

        return chunk;
    }
}

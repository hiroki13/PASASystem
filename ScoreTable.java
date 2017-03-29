/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author hiroki
 */
public class ScoreTable {
    final private int N_CASES = Config.N_CASES;
    final public int[][][][] localFeatIDs;
    final public float[][][] localScores;
    final public int[][][][][][][] globalFeatIDs;
    final public float[][][][][][] globalScores;

    public ScoreTable(int nPrds, int nArgs) {
        localFeatIDs = new int[nPrds][nArgs][N_CASES][];
        localScores = new float[nPrds][nArgs][N_CASES];
        globalFeatIDs = new int[nPrds][nArgs][N_CASES][nPrds][nArgs][N_CASES][];
        globalScores = new float[nPrds][nArgs][N_CASES][nPrds][nArgs][N_CASES];
    }
}

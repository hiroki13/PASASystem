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
    final public int[][][][][][][] featIDs;
    final public float[][][][][][] scores;

    public ScoreTable(int nPrds, int nArgs) {
        featIDs = new int[nPrds][nArgs][N_CASES][nPrds][nArgs][N_CASES][];
        scores = new float[nPrds][nArgs][N_CASES][nPrds][nArgs][N_CASES];
    }
}

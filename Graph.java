/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author hiroki
 */
public class Graph {

    public int nPrds;
    public int nCases;

    public int[][] bestGraph;
    public int[] bestGraphFeatIDs;
    public float bestGraphScore;

    public Graph() {}
    
    public void setBestGraphFeatIDs() {}

    final public static boolean isEqualGraph(int[][] graph1, int[][] graph2) {
        for (int i=0; i<graph1.length; ++i)
            for (int j=0; j<graph1[i].length; ++j)
                if (graph1[i][j] != graph2[i][j])
                    return false;
        return true;
    }
        

}

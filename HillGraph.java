/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author hiroki
 */
public class HillGraph extends Graph {

    public HillGraph(int nPrds, int nArgs, int nCases) {
        this.nPrds = nPrds;
        this.nCases = nCases;
        this.bestGraph = new int[nPrds][nCases];
    }
    
    public HillGraph(int[][] graph, int[] featIDs) {
        this.nPrds = graph.length;
        this.nCases = graph[0].length;
        this.bestGraph = graph;
        this.bestGraphFeatIDs = featIDs;
    }
    
}

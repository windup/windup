package org.jboss.windup.graph.test.polymorf;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;

/**
 * Creates test graphs with some data.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class GraphCreator
{
    public static Graph createFamilyGraph()
    {
        Graph g = new TinkerGraph();

        // Manipulation through BluePrints API.

        // Ja
        Vertex oz = g.addVertex(null);
        oz.setProperty("name", "Ondrej Zizka");
        oz.setProperty("man", true);

        // Mamka
        Vertex sz = g.addVertex(null);
        sz.setProperty("name", "Stanislava Zizkova");
        sz.setProperty("man", false);
        g.addEdge(null, sz, oz, "parent");

        // Deda
        Vertex jz0 = g.addVertex(null);
        jz0.setProperty("name", "Josef Zizka");
        jz0.setProperty("man", true);

        // Babicka
        Vertex bab = g.addVertex(null);
        bab.setProperty("name", "Jaroslava Zizkova");
        bab.setProperty("man", false);
        g.addEdge(null, jz0, bab, "married");
        g.addEdge(null, bab, jz0, "married");

        // Tata
        Vertex jz1 = g.addVertex(null);
        jz1.setProperty("name", "Josef Zizka");
        jz1.setProperty("man", true);
        g.addEdge(null, jz1, oz, "parent");
        g.addEdge(null, sz, jz1, "married");
        g.addEdge(null, jz1, sz, "married");

        // Bracha
        Vertex jz2 = g.addVertex(null);
        jz2.setProperty("name", "Josef Zizka");
        jz2.setProperty("man", true);
        g.addEdge(null, jz1, jz2, "parent");

        // Pepicek
        Vertex jz3 = g.addVertex(null);
        jz3.setProperty("name", "Josef Zizka");
        jz3.setProperty("man", true);
        g.addEdge(null, jz2, jz3, "parent");

        return g;
    }

}
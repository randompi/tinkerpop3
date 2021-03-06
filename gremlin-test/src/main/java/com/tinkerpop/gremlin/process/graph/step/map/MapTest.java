package com.tinkerpop.gremlin.process.graph.step.map;

import com.tinkerpop.gremlin.LoadGraphWith;
import com.tinkerpop.gremlin.process.AbstractGremlinProcessTest;
import com.tinkerpop.gremlin.process.Traversal;
import com.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import java.util.List;

import static com.tinkerpop.gremlin.LoadGraphWith.GraphData.CLASSIC;
import static org.junit.Assert.*;

/**
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public abstract class MapTest extends AbstractGremlinProcessTest {
    public abstract Traversal<Vertex, String> get_g_v1_mapXnameX(final Object v1Id);

    public abstract Traversal<Vertex, Integer> get_g_v1_outE_label_mapXlengthX(final Object v1Id);

    public abstract Traversal<Vertex, Integer> get_g_v1_out_mapXnameX_mapXlengthX(final Object v1Id);

    public abstract Traversal<Vertex, String> get_g_V_asXaX_out_mapXa_nameX();

    @Test
    @LoadGraphWith(CLASSIC)
    public void g_v1_mapXnameX() {
        final Traversal<Vertex, String> traversal = get_g_v1_mapXnameX(convertToVertexId("marko"));
        printTraversalForm(traversal);
        assertEquals(traversal.next(), "marko");
        assertFalse(traversal.hasNext());
    }

    @Test
    @LoadGraphWith(CLASSIC)
    public void g_v1_outE_label_mapXlengthX() {
        final Traversal<Vertex, Integer> traversal = get_g_v1_outE_label_mapXlengthX(convertToVertexId("marko"));
        printTraversalForm(traversal);
        List<Integer> lengths = traversal.toList();
        assertTrue(lengths.contains("created".length()));
        assertTrue(lengths.contains("knows".length()));
        assertEquals(lengths.size(), 3);
        assertFalse(traversal.hasNext());
    }

    @Test
    @LoadGraphWith(CLASSIC)
    public void g_v1_out_mapXnameX_mapXlengthX() {
        final Traversal<Vertex, Integer> traversal = get_g_v1_out_mapXnameX_mapXlengthX(convertToVertexId("marko"));
        printTraversalForm(traversal);
        final List<Integer> lengths = traversal.toList();
        assertTrue(lengths.contains("josh".length()));
        assertTrue(lengths.contains("vadas".length()));
        assertTrue(lengths.contains("lop".length()));
        assertEquals(lengths.size(), 3);
    }

    @Test
    @LoadGraphWith(CLASSIC)
    public void g_V_asXaX_out_mapXa_nameX() {
        int marko = 0;
        int peter = 0;
        int josh = 0;
        int other = 0;

        final Traversal<Vertex, String> traversal = get_g_V_asXaX_out_mapXa_nameX();
        printTraversalForm(traversal);
        while (traversal.hasNext()) {
            final String name = traversal.next();
            if (name.equals("marko")) marko++;
            else if (name.equals("peter")) peter++;
            else if (name.equals("josh")) josh++;
            else other++;
        }
        assertEquals(marko, 3);
        assertEquals(josh, 2);
        assertEquals(peter, 1);
        assertEquals(other, 0);
    }

    public static class JavaMapTest extends MapTest {
        public JavaMapTest() {
            requiresGraphComputer = false;
        }

        @Override
        public Traversal<Vertex, String> get_g_v1_mapXnameX(final Object v1Id) {
            return g.v(v1Id).<String>map(v -> v.get().value("name"));
        }

        @Override
        public Traversal<Vertex, Integer> get_g_v1_outE_label_mapXlengthX(final Object v1Id) {
            return g.v(v1Id).outE().label().map(l -> l.get().length());
        }

        @Override
        public Traversal<Vertex, Integer> get_g_v1_out_mapXnameX_mapXlengthX(final Object v1Id) {
            return g.v(v1Id).out().map(v -> v.get().value("name")).map(n -> n.get().toString().length());
        }

        @Override
        public Traversal<Vertex, String> get_g_V_asXaX_out_mapXa_nameX() {
            return g.V().as("a").out().<String>map(v -> ((Vertex) v.getPath().get("a")).value("name")).trackPaths();
        }
    }

    public static class JavaComputerMapTest extends MapTest {
        public JavaComputerMapTest() {
            requiresGraphComputer = true;
        }

        @Override
        public Traversal<Vertex, String> get_g_v1_mapXnameX(final Object v1Id) {
            return g.v(v1Id).<String>map(v -> v.get().value("name")).submit(g.compute());
        }

        @Override
        public Traversal<Vertex, Integer> get_g_v1_outE_label_mapXlengthX(final Object v1Id) {
            return g.v(v1Id).<String>outE().label().map(l -> l.get().length()).submit(g.compute());
        }

        @Override
        public Traversal<Vertex, Integer> get_g_v1_out_mapXnameX_mapXlengthX(final Object v1Id) {
            return g.v(v1Id).<String>out().map(v -> v.get().value("name")).map(n -> n.get().toString().length()).submit(g.compute());
        }

        @Override
        public Traversal<Vertex, String> get_g_V_asXaX_out_mapXa_nameX() {
            // TODO: Doesn't work for graph computer because of DetachedElements not having properties
            return g.V().as("a").out().<String>map(t -> ((Vertex) t.getPath().get("a")).value("name")).trackPaths();
        }
    }
}

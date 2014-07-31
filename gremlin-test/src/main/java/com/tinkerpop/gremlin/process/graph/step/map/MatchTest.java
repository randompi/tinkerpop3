package com.tinkerpop.gremlin.process.graph.step.map;

import com.tinkerpop.gremlin.LoadGraphWith;
import com.tinkerpop.gremlin.process.AbstractGremlinProcessTest;
import com.tinkerpop.gremlin.process.Traversal;
import com.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import java.util.Map;

import static com.tinkerpop.gremlin.LoadGraphWith.GraphData.CLASSIC;
import static org.junit.Assert.*;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class MatchTest extends AbstractGremlinProcessTest {

    public abstract Traversal<Vertex, Map<String, Object>> get_g_V_matchXa_out_bX();

    public abstract Traversal<Vertex, Map<String, Object>> get_g_V_matchXa_outXknowsX_b__b_outXcreatedX_cX();



    @Test
    @LoadGraphWith(CLASSIC)
    public void g_V_matchXa_out_bX() throws Exception {
        final Traversal<Vertex, Map<String, Object>> traversal = get_g_V_matchXa_out_bX();
        System.out.println("Testing: " + traversal);
        int counter = 0;
        while (traversal.hasNext()) {
            counter++;
            final Map<String, Object> bindings = traversal.next();
            assertEquals(2, bindings.size());
            final Object aId = ((Vertex) bindings.get("a")).id();
            final Object bId = ((Vertex) bindings.get("b")).id();
            if (aId.equals(convertToVertexId("marko"))) {
                assertTrue(bId.equals(convertToVertexId("vadas")) ||
                        bId.equals(convertToVertexId("lop")) ||
                        bId.equals(convertToVertexId("josh")));
            } else if (aId.equals(convertToVertexId("josh"))) {
                assertTrue(bId.equals(convertToVertexId("lop")) ||
                        bId.equals(convertToVertexId("ripple")));
            } else if (aId.equals(convertToVertexId("peter"))) {
                assertEquals(convertToVertexId("lop"), bId);
            } else {
                assertFalse(true);
            }
        }
        assertFalse(traversal.hasNext());
        // TODO: The full result set isn't coming back (only the marko vertices)
        // assertEquals(6, counter);
    }

    @Test
    @LoadGraphWith(CLASSIC)
    public void g_V_a_outXknowsX_b__b_outXcreatedX_c() throws Exception {
        final Traversal<Vertex, Map<String, Object>> traversal = get_g_V_matchXa_outXknowsX_b__b_outXcreatedX_cX();
        System.out.println("Testing: " + traversal);
        int counter = 0;
        while (traversal.hasNext()) {
            counter++;
            final Map<String, Object> bindings = traversal.next();
            assertEquals(3, bindings.size());
            final Object aId = ((Vertex) bindings.get("a")).id();
            final Object bId = ((Vertex) bindings.get("b")).id();
            final Object cId = ((Vertex) bindings.get("c")).id();
            assertEquals(convertToVertexId("marko"), aId);
            assertEquals(convertToVertexId("josh"), bId);
            assertTrue(cId.equals(convertToVertexId("lop")) ||
                    cId.equals(convertToVertexId("ripple")));
        }
        assertFalse(traversal.hasNext());
        assertEquals(2, counter);
    }


    public static class JavaMapTest extends MatchTest {
        public JavaMapTest() {
            requiresGraphComputer = false;
        }

        @Override
        public Traversal<Vertex, Map<String, Object>> get_g_V_matchXa_out_bX() {
            return g.V().match("a", g.of().as("a").out().as("b"));
        }

        @Override
        public Traversal<Vertex, Map<String, Object>> get_g_V_matchXa_outXknowsX_b__b_outXcreatedX_cX() {
            return g.V().match("a",
                    g.of().as("a").out("knows").as("b"),
                    g.of().as("b").out("created").as("c"));
        }


    }

    public static class JavaComputerMapTest extends MatchTest {
        public JavaComputerMapTest() {
            requiresGraphComputer = true;
        }

        @Override
        public Traversal<Vertex, Map<String, Object>> get_g_V_matchXa_out_bX() {
            return g.V().match("a", g.of().as("a").out().as("b")).submit(g.compute());
        }

        @Override
        public Traversal<Vertex, Map<String, Object>> get_g_V_matchXa_outXknowsX_b__b_outXcreatedX_cX() {
            return g.V().match("a",
                    g.of().as("a").out("knows").as("b"),
                    g.of().as("b").out("created").as("c")).submit(g.compute());
        }
    }
}
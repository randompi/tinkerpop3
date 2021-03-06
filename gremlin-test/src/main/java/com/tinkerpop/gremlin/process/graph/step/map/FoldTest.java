package com.tinkerpop.gremlin.process.graph.step.map;

import com.tinkerpop.gremlin.AbstractGremlinTest;
import com.tinkerpop.gremlin.LoadGraphWith;
import com.tinkerpop.gremlin.process.Traversal;
import com.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.tinkerpop.gremlin.LoadGraphWith.GraphData.CLASSIC;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class FoldTest extends AbstractGremlinTest {

    public abstract Traversal<Vertex, List<Vertex>> get_g_V_fold();

    public abstract Traversal<Vertex, Vertex> get_g_V_fold_unfold();

    public abstract Traversal<Vertex, Integer> get_g_V_hasXageX_foldX0_plusX();

    @Test
    @LoadGraphWith(CLASSIC)
    public void g_V_fold() {
        final Traversal<Vertex, List<Vertex>> traversal = get_g_V_fold();
        printTraversalForm(traversal);
        List<Vertex> list = traversal.next();
        assertFalse(traversal.hasNext());
        Set<Vertex> vertices = new HashSet<>(list);
        assertEquals(6, vertices.size());
    }

    @Test
    @LoadGraphWith(CLASSIC)
    public void g_V_fold_unfold() {
        final Traversal<Vertex, Vertex> traversal = get_g_V_fold_unfold();
        printTraversalForm(traversal);
        int count = 0;
        Set<Vertex> vertices = new HashSet<>();
        while (traversal.hasNext()) {
            vertices.add(traversal.next());
            count++;
        }
        assertFalse(traversal.hasNext());
        assertEquals(6, count);
        assertEquals(6, vertices.size());
    }

    @Test
    @LoadGraphWith(CLASSIC)
    public void g_V_hasXageX_foldX0_plusX() {
        final Traversal<Vertex, Integer> traversal = get_g_V_hasXageX_foldX0_plusX();
        printTraversalForm(traversal);
        final Integer ageSum = traversal.next();
        assertFalse(traversal.hasNext());
        assertEquals(Integer.valueOf(123), ageSum);
    }

    public static class JavaFoldTest extends FoldTest {

        public Traversal<Vertex, List<Vertex>> get_g_V_fold() {
            return g.V().fold();
        }

        public Traversal<Vertex, Vertex> get_g_V_fold_unfold() {
            return g.V().fold().unfold();
        }

        public Traversal<Vertex, Integer> get_g_V_hasXageX_foldX0_plusX() {
            return g.V().<Vertex>has("age").fold(0, (seed, v) -> seed + v.<Integer>value("age"));
        }
    }
}

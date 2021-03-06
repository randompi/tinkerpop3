package com.tinkerpop.gremlin.process.graph.step.sideEffect;

import com.tinkerpop.gremlin.AbstractGremlinTest;
import com.tinkerpop.gremlin.LoadGraphWith;
import com.tinkerpop.gremlin.process.Traversal;
import com.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import java.util.Map;

import static com.tinkerpop.gremlin.LoadGraphWith.GraphData.CLASSIC;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class SideEffectCapTest extends AbstractGremlinTest {
    public abstract Traversal<Vertex, Map<String, Long>> get_g_V_hasXageX_groupCountXnameX_asXaX_out_capXaX();

    @Test
    @LoadGraphWith(CLASSIC)
    public void g_V_hasXageX_groupCountXnameX_asXaX_out_capXaX() {
        final Traversal<Vertex, Map<String, Long>> traversal = get_g_V_hasXageX_groupCountXnameX_asXaX_out_capXaX();
        printTraversalForm(traversal);
        Map<String, Long> map = traversal.next();
        assertFalse(traversal.hasNext());
        assertEquals(map.get("marko"), new Long(1l));
        assertEquals(map.get("vadas"), new Long(1l));
        assertEquals(map.get("peter"), new Long(1l));
        assertEquals(map.get("josh"), new Long(1l));
        assertEquals(map.size(), 4);
    }

    public static class JavaSideEffectCapTest extends SideEffectCapTest {

        public Traversal<Vertex, Map<String, Long>> get_g_V_hasXageX_groupCountXnameX_asXaX_out_capXaX() {
            return g.V().<Vertex>has("age").groupCount(v -> v.value("name")).as("a").out().cap("a");
        }
    }

    public static class JavaComputerSideEffectCapTest extends SideEffectCapTest {
        public Traversal<Vertex, Map<String, Long>> get_g_V_hasXageX_groupCountXnameX_asXaX_out_capXaX() {
            return g.V().<Vertex>has("age").groupCount(v -> v.value("name")).as("a").out().<Map<String, Long>>cap("a").submit(g.compute());
        }
    }
}

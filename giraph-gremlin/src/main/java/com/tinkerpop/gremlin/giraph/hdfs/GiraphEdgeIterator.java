package com.tinkerpop.gremlin.giraph.hdfs;

import com.google.common.collect.Iterators;
import com.tinkerpop.gremlin.giraph.structure.GiraphEdge;
import com.tinkerpop.gremlin.giraph.structure.GiraphGraph;
import com.tinkerpop.gremlin.giraph.structure.util.GiraphInternalVertex;
import com.tinkerpop.gremlin.process.util.FastNoSuchElementException;
import com.tinkerpop.gremlin.structure.Direction;
import com.tinkerpop.gremlin.structure.Edge;
import com.tinkerpop.gremlin.tinkergraph.structure.TinkerEdge;
import org.apache.giraph.io.VertexInputFormat;
import org.apache.hadoop.fs.Path;

import java.io.IOException;
import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class GiraphEdgeIterator extends GiraphElementIterator<GiraphEdge> {

    private Iterator<Edge> edgeIterator = Iterators.emptyIterator();

    public GiraphEdgeIterator(final GiraphGraph graph, final VertexInputFormat inputFormat, final Path path) throws IOException {
        super(graph, inputFormat, path);
    }

    public GiraphEdgeIterator(final GiraphGraph graph) throws IOException {
        super(graph);
    }

    public GiraphEdge next() {
        try {
            while (true) {
                if (this.edgeIterator.hasNext())
                    return new GiraphEdge((TinkerEdge) this.edgeIterator.next(), this.graph);
                if (this.readers.isEmpty())
                    throw FastNoSuchElementException.instance();
                if (this.readers.peek().nextVertex()) {
                    this.edgeIterator = ((GiraphInternalVertex) this.readers.peek().getCurrentVertex()).getTinkerVertex().edges(Direction.OUT, Integer.MAX_VALUE);
                } else {
                    this.readers.remove();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public boolean hasNext() {
        try {
            while (true) {
                if (this.edgeIterator.hasNext())
                    return true;
                if (this.readers.isEmpty())
                    return false;
                if (this.readers.peek().nextVertex()) {
                    this.edgeIterator = ((GiraphInternalVertex) this.readers.peek().getCurrentVertex()).getTinkerVertex().edges(Direction.OUT, Integer.MAX_VALUE);
                } else {
                    this.readers.remove();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
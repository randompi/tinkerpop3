package com.tinkerpop.gremlin.neo4j.structure;

import com.tinkerpop.gremlin.process.graph.GraphTraversal;
import com.tinkerpop.gremlin.process.graph.step.map.StartStep;
import com.tinkerpop.gremlin.process.graph.util.DefaultGraphTraversal;
import com.tinkerpop.gremlin.structure.Direction;
import com.tinkerpop.gremlin.structure.Edge;
import com.tinkerpop.gremlin.structure.Graph;
import com.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.gremlin.structure.util.ElementHelper;
import com.tinkerpop.gremlin.structure.util.StringFactory;
import com.tinkerpop.gremlin.structure.util.wrapped.WrappedVertex;
import com.tinkerpop.gremlin.util.StreamFactory;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Relationship;

import java.util.Iterator;

/**
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public class Neo4jVertex extends Neo4jElement implements Vertex, WrappedVertex<Node> {
    public Neo4jVertex(final Node node, final Neo4jGraph graph) {
        super(graph);
        this.baseElement = node;
    }

    @Override
    public void remove() {
        this.graph.tx().readWrite();

        try {
            final Node node = (Node) this.baseElement;
            for (final Relationship relationship : node.getRelationships(org.neo4j.graphdb.Direction.BOTH)) {
                relationship.delete();
            }
            node.delete();
        } catch (NotFoundException ignored) {
            // this one happens if the vertex is committed
        } catch (IllegalStateException ignored) {
            // this one happens if the vertex is still chilling in the tx
        }
    }

    @Override
    public Edge addEdge(final String label, final Vertex inVertex, final Object... keyValues) {
        if (label == null)
            throw Edge.Exceptions.edgeLabelCanNotBeNull();
        ElementHelper.legalPropertyKeyValueArray(keyValues);
        if (ElementHelper.getIdValue(keyValues).isPresent())
            throw Edge.Exceptions.userSuppliedIdsNotSupported();

        this.graph.tx().readWrite();
        final Node node = (Node) this.baseElement;
        final Neo4jEdge edge = new Neo4jEdge(node.createRelationshipTo(((Neo4jVertex) inVertex).getBaseVertex(),
                DynamicRelationshipType.withName(label)), this.graph);
        ElementHelper.attachProperties(edge, keyValues);
        return edge;
    }

    @Override
    public Iterator<Vertex> vertices(final Direction direction, final int branchFactor, final String... labels) {
        this.graph.tx().readWrite();
        return (Iterator) StreamFactory.stream(Neo4jHelper.getVertices(this, direction, labels)).limit(branchFactor).iterator();
    }

    @Override
    public Iterator<Edge> edges(final Direction direction, final int branchFactor, final String... labels) {
        this.graph.tx().readWrite();
        return (Iterator) StreamFactory.stream(Neo4jHelper.getEdges(this, direction, labels)).limit(branchFactor).iterator();
    }

    public GraphTraversal<Vertex, Vertex> start() {
        final GraphTraversal<Vertex, Vertex> traversal = new DefaultGraphTraversal<>(this.graph);
        return (GraphTraversal) traversal.addStep(new StartStep<>(traversal, this));
    }

    public Node getBaseVertex() {
        return (Node) this.baseElement;
    }

    public String toString() {
        return StringFactory.vertexString(this);
    }


}

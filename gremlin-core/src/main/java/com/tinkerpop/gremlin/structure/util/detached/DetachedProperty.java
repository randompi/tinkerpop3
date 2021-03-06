package com.tinkerpop.gremlin.structure.util.detached;

import com.tinkerpop.gremlin.structure.Edge;
import com.tinkerpop.gremlin.structure.Element;
import com.tinkerpop.gremlin.structure.Graph;
import com.tinkerpop.gremlin.structure.Property;
import com.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.gremlin.structure.util.ElementHelper;
import com.tinkerpop.gremlin.structure.util.StringFactory;
import com.tinkerpop.gremlin.util.StreamFactory;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Optional;

/**
 * @author Stephen Mallette (http://stephen.genoprime.com)
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class DetachedProperty<V> implements Property, Serializable {

    String key;
    V value;
    DetachedElement element;
    int hashCode;

    private DetachedProperty() {

    }

    public DetachedProperty(final String key, final V value, final DetachedElement element) {
        if (null == key) throw Graph.Exceptions.argumentCanNotBeNull("key");
        if (null == value) throw Graph.Exceptions.argumentCanNotBeNull("value");
        if (null == element) throw Graph.Exceptions.argumentCanNotBeNull("element");

        this.key = key;
        this.value = value;
        this.element = element;
        this.hashCode = super.hashCode();
    }

    private DetachedProperty(final Property property) {
        if (null == property) throw Graph.Exceptions.argumentCanNotBeNull("property");

        this.key = property.key();
        this.value = (V) property.value();
        this.hashCode = property.hashCode();
        final Element element = property.getElement();
        this.element = element instanceof Vertex ?
                DetachedVertex.detach((Vertex) element) :
                DetachedEdge.detach((Edge) element);
    }

    public boolean isPresent() {
        return true;
    }

    public boolean isHidden() {
        return Graph.Key.isHidden(this.key);
    }

    public String key() {
        return Graph.Key.unHide(this.key);
    }

    public V value() {
        return this.value;
    }

    public Element getElement() {
        return this.element;
    }

    public void remove() {
        throw new UnsupportedOperationException("Detached properties are readonly: " + this.toString());
    }

    @Override
    public String toString() {
        return StringFactory.propertyString(this);
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(final Object object) {
        return ElementHelper.areEqual(this, object);
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    public Property<V> attach(final Vertex hostVertex) {
        if (this.getElement() instanceof Vertex) {
            return Optional.<Property<V>>of(hostVertex.property(this.key)).orElseThrow(() -> new IllegalStateException("The detached property could not be be found at the provided vertex: " + this));
        } else {
            final String label = this.getElement().label();
            final Object id = this.getElement().id();
            return StreamFactory.stream((Iterator<Edge>) hostVertex.outE(label))
                    .filter(e -> e.id().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("The detached property could not be be found at the provided vertex's edges: " + this))
                    .property(this.key());

        }
    }

    public Property<V> attach(final Graph graph) {
        final Element element = (this.getElement() instanceof Vertex) ?
                graph.v(this.getElement().id()) :
                graph.e(this.getElement().id());
        return Optional.<Property<V>>of(element.property(this.key)).orElseThrow(() -> new IllegalStateException("The detached property could not be found in the provided graph: " + this));
    }

    public static DetachedProperty detach(final Property property) {
        return new DetachedProperty(property);
    }
}

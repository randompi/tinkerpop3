package com.tinkerpop.gremlin.process.graph.step.map;

import com.tinkerpop.gremlin.process.Traversal;
import com.tinkerpop.gremlin.process.Traverser;
import com.tinkerpop.gremlin.process.graph.marker.Reversible;
import com.tinkerpop.gremlin.process.util.UnTraverserIterator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class OrderStep<S> extends FlatMapStep<S, S> implements Reversible {

    public Comparator<Traverser<S>> comparator;

    public OrderStep(final Traversal traversal, final Comparator<Traverser<S>> comparator) {
        super(traversal);
        this.comparator = comparator;
        this.setFunction(traverser -> {
            final List<Traverser<S>> list = new ArrayList<>();
            list.add(traverser);
            this.starts.forEachRemaining(list::add);
            Collections.sort(list, this.comparator);
            return new UnTraverserIterator<>(list.iterator());
        });
    }
}

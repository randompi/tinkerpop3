package com.tinkerpop.gremlin.process.oltp.filter;

import com.tinkerpop.gremlin.process.Traversal;
import com.tinkerpop.gremlin.structure.AnnotatedValue;
import com.tinkerpop.gremlin.structure.Element;
import com.tinkerpop.gremlin.structure.query.util.HasContainer;
import com.tinkerpop.gremlin.util.GremlinHelper;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class IntervalPipe<S> extends FilterPipe<S> {

    public HasContainer startContainer;
    public HasContainer endContainer;

    public IntervalPipe(final Traversal pipeline, final HasContainer startContainer, final HasContainer endContainer) {
        super(pipeline);
        this.startContainer = startContainer;
        this.endContainer = endContainer;
        this.setPredicate(holder -> {
            final S s = holder.get();
            if (s instanceof Element)
                return startContainer.test((Element) s) && endContainer.test((Element) s);
            else if (s instanceof AnnotatedValue)
                return startContainer.test((AnnotatedValue) s) && endContainer.test((AnnotatedValue) s);
            else
                throw new IllegalArgumentException("The provided class can not be check with has(): " + s.getClass());
        });
    }

    public String toString() {
        return GremlinHelper.makePipeString(this, this.startContainer, this.endContainer);
    }
}
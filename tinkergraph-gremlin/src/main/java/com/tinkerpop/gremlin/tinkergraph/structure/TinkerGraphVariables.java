package com.tinkerpop.gremlin.tinkergraph.structure;

import com.tinkerpop.gremlin.structure.Graph;
import com.tinkerpop.gremlin.structure.util.GraphVariableHelper;
import com.tinkerpop.gremlin.structure.util.StringFactory;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class TinkerGraphVariables implements Graph.Variables, Serializable {

    private final Map<String, Object> variables = new ConcurrentHashMap<>();

    public TinkerGraphVariables() {

    }

    public Set<String> keys() {
        return this.variables.keySet();
    }

    public <R> Optional<R> get(final String key) {
        return Optional.ofNullable((R) this.variables.get(key));
    }

    public void remove(final String key) {
        this.variables.remove(key);
    }

    public void set(final String key, final Object value) {
        GraphVariableHelper.validateVariable(key, value);
        this.variables.put(key, value);
    }

    public String toString() {
        return StringFactory.graphVariablesString(this);
    }
}

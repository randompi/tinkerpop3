package com.tinkerpop.gremlin.process.computer.ranking.pagerank;

import com.tinkerpop.gremlin.process.computer.MapReduce;
import com.tinkerpop.gremlin.process.computer.ranking.pagerank.PageRankVertexProgram;
import com.tinkerpop.gremlin.structure.Property;
import com.tinkerpop.gremlin.structure.Vertex;
import org.apache.commons.configuration.Configuration;
import org.javatuples.Pair;

import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class PageRankMapReduce implements MapReduce<Object, Double, Object, Double, Iterator<Pair<Object, Double>>> {

    public static final String PAGE_RANK_SIDE_EFFECT_KEY = "gremlin.pageRank.sideEffectKey";
    public static final String DEFAULT_SIDE_EFFECT_KEY = "pageRank";

    private String sideEffectKey = DEFAULT_SIDE_EFFECT_KEY;

    public PageRankMapReduce() {

    }

    public PageRankMapReduce(final String sideEffectKey) {
        this.sideEffectKey = sideEffectKey;
    }

    @Override
    public void storeState(final Configuration configuration) {
        configuration.setProperty(PAGE_RANK_SIDE_EFFECT_KEY, this.sideEffectKey);
    }

    @Override
    public void loadState(final Configuration configuration) {
        this.sideEffectKey = configuration.getString(PAGE_RANK_SIDE_EFFECT_KEY, DEFAULT_SIDE_EFFECT_KEY);
    }

    @Override
    public boolean doStage(final Stage stage) {
        return stage.equals(Stage.MAP);
    }

    @Override
    public void map(final Vertex vertex, final MapEmitter<Object, Double> emitter) {
        final Property pageRank = vertex.property(PageRankVertexProgram.PAGE_RANK);
        if (pageRank.isPresent()) {
            emitter.emit(vertex.id(), (Double) pageRank.value());
        }
    }

    @Override
    public Iterator<Pair<Object, Double>> generateSideEffect(final Iterator<Pair<Object, Double>> keyValues) {
        return keyValues;
    }

    @Override
    public String getSideEffectKey() {
        return this.sideEffectKey;
    }
}
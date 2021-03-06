package com.tinkerpop.gremlin;

import com.tinkerpop.gremlin.structure.FeatureRequirement;
import com.tinkerpop.gremlin.structure.Graph.Features.EdgePropertyFeatures;
import com.tinkerpop.gremlin.structure.Graph.Features.VertexPropertyFeatures;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

import static com.tinkerpop.gremlin.structure.Graph.Features.DataTypeFeatures.FEATURE_FLOAT_VALUES;
import static com.tinkerpop.gremlin.structure.Graph.Features.DataTypeFeatures.FEATURE_INTEGER_VALUES;
import static com.tinkerpop.gremlin.structure.Graph.Features.DataTypeFeatures.FEATURE_STRING_VALUES;

/**
 * Annotations to define a graph example to load from test resources prior to test execution.  This annotation is
 * for use only with test that extend from {@link AbstractGremlinTest}.
 *
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LoadGraphWith {

    public enum GraphData {
        CLASSIC,
        GRATEFUL;

        private static final List<FeatureRequirement> featuresRequiredByClassic = new ArrayList<FeatureRequirement>(){{
            add(FeatureRequirement.Factory.create(FEATURE_STRING_VALUES, VertexPropertyFeatures.class));
            add(FeatureRequirement.Factory.create(FEATURE_INTEGER_VALUES, VertexPropertyFeatures.class));
            add(FeatureRequirement.Factory.create(FEATURE_FLOAT_VALUES, EdgePropertyFeatures.class));
        }};

        private static final List<FeatureRequirement> featuresRequiredByGrateful = new ArrayList<FeatureRequirement>() {{
            add(FeatureRequirement.Factory.create(FEATURE_STRING_VALUES, VertexPropertyFeatures.class));
            add(FeatureRequirement.Factory.create(FEATURE_INTEGER_VALUES, VertexPropertyFeatures.class));
        }};

        public String location() {
            switch (this) {
                case CLASSIC:
                    return RESOURCE_PATH_PREFIX + "tinkerpop-classic.gio";
                case GRATEFUL:
                    return RESOURCE_PATH_PREFIX + "grateful-dead.gio";
            }

            throw new RuntimeException("No file for this GraphData type");
        }

        public List<FeatureRequirement> featuresRequired() {
            switch (this) {
                case CLASSIC:
                    return featuresRequiredByClassic;
                case GRATEFUL:
                    return featuresRequiredByGrateful;
            }

            throw new RuntimeException("No features for this GraphData type");
        }
    }

    public static final String RESOURCE_PATH_PREFIX = "/com/tinkerpop/gremlin/structure/util/io/kryo/";

    /**
     * The name of the resource to load with full path.
     */
    public GraphData value() default GraphData.CLASSIC;

}

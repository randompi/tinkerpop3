package com.tinkerpop.gremlin.process.computer.util;

import com.tinkerpop.gremlin.process.computer.GraphComputer;
import com.tinkerpop.gremlin.process.computer.VertexProgram;
import com.tinkerpop.gremlin.structure.Graph;

import java.lang.reflect.Method;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class GraphComputerHelper {

    public static void validateProgramOnComputer(final GraphComputer computer, final VertexProgram vertexProgram) {
        final GraphComputer.Features graphComputerFeatures = computer.getFeatures();
        final VertexProgram.Features vertexProgramFeatures = vertexProgram.getFeatures();

        for (final Method method : VertexProgram.Features.class.getMethods()) {
            if (method.getName().startsWith("requires")) {
                final boolean supports;
                final boolean requires;
                try {
                    supports = (boolean) GraphComputer.Features.class.getMethod(method.getName().replace("requires", "supports")).invoke(graphComputerFeatures);
                    requires = (boolean) method.invoke(vertexProgramFeatures);
                } catch (final Exception e) {
                    throw new IllegalStateException("A reflection exception has occurred: " + e.getMessage(), e);
                }
                if (requires && !supports)
                    throw new IllegalStateException("The vertex program can not be executed on the graph computer: " + method.getName());
            }
        }
    }

    public static void validateComputeArguments(Class... graphComputerClass) {
        if (graphComputerClass.length > 1)
            throw Graph.Exceptions.onlyOneOrNoGraphComputerClass();
    }

}

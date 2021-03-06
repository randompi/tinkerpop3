package com.tinkerpop.gremlin.giraph.groovy.plugin;

import com.tinkerpop.gremlin.giraph.process.computer.util.GiraphComputerHelper;
import com.tinkerpop.gremlin.giraph.structure.GiraphGraph;
import com.tinkerpop.gremlin.groovy.engine.function.GSSupplier;
import com.tinkerpop.gremlin.groovy.plugin.RemoteAcceptor;
import com.tinkerpop.gremlin.process.computer.ComputerResult;
import com.tinkerpop.gremlin.process.computer.traversal.TraversalVertexProgram;
import com.tinkerpop.gremlin.process.computer.traversal.step.map.ComputerResultStep;
import com.tinkerpop.gremlin.process.graph.GraphTraversal;
import com.tinkerpop.gremlin.process.graph.util.DefaultGraphTraversal;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.FileConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.codehaus.groovy.tools.shell.Groovysh;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public class GiraphRemoteAcceptor implements RemoteAcceptor {

    private static final String PREFIX_SCRIPT =
            "import " + GiraphGraph.class.getPackage().getName() + ".*\n" +
                    "import " + GiraphComputerHelper.class.getPackage().getName() + ".*\n" +
                    "g = GiraphGraph.open()\n" +
                    "traversal = ";

    private static final String POSTFIX_SCRIPT = "\nGiraphComputerHelper.prepareTraversalForComputer(traversal)\n" +
            "traversal\n";

    //TODO: might not always be 'g' cause of the variable bindings

    private GiraphGraph giraphGraph;
    private Groovysh shell;

    public GiraphRemoteAcceptor(final Groovysh shell) {
        this.shell = shell;
    }

    @Override
    public Object connect(final List<String> args) {
        if (args.size() == 0) {
            this.giraphGraph = GiraphGraph.open(new BaseConfiguration());
            this.shell.getInterp().getContext().setProperty("g", this.giraphGraph);
        }
        if (args.size() == 1) {
            try {
                final FileConfiguration configuration = new PropertiesConfiguration();
                configuration.load(new File(args.get(0)));
                this.giraphGraph = GiraphGraph.open(configuration);
                this.shell.getInterp().getContext().setProperty("g", this.giraphGraph);
            } catch (final Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        } else if (args.size() == 2) {
            try {
                final FileConfiguration configuration = new PropertiesConfiguration();
                configuration.load(new File(args.get(0)));
                this.giraphGraph = GiraphGraph.open(configuration);
                this.shell.getInterp().getContext().setProperty(args.get(1), this.giraphGraph);
            } catch (final Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        return this.giraphGraph;
    }

    @Override
    public Object configure(final List<String> args) {
        for (int i = 0; i < args.size(); i = i + 2) {
            this.giraphGraph.variables().getConfiguration().setProperty(args.get(i), args.get(i + 1));
        }
        return this.giraphGraph;
    }

    @Override
    public Object submit(final List<String> args) {
        try {
            TraversalVertexProgram vertexProgram = TraversalVertexProgram.build().traversal(new GSSupplier<>(PREFIX_SCRIPT + args.get(0) + POSTFIX_SCRIPT)).create();
            final ComputerResult result = this.giraphGraph.compute().program(vertexProgram).submit().get();
            this.shell.getInterp().getContext().setProperty("g", result.getGraph());
            this.shell.getInterp().getContext().setProperty("sideEffects", result.getSideEffects());

            final GraphTraversal traversal1 = new DefaultGraphTraversal<>();
            traversal1.addStep(new ComputerResultStep<>(traversal1, result.getSideEffects(), vertexProgram));
            this.shell.getInterp().getContext().setProperty("_l", traversal1);

            final GraphTraversal traversal2 = new DefaultGraphTraversal<>();
            traversal2.addStep(new ComputerResultStep<>(traversal2, result.getSideEffects(), vertexProgram));
            traversal2.range(0, 19);
            return traversal2;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {
        this.giraphGraph.close();
    }

    public GiraphGraph getGraph() {
        return this.giraphGraph;
    }

    public String toString() {
        return "GiraphRemoteAcceptor[" + this.giraphGraph + "]";
    }
}

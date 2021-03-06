package com.tinkerpop.gremlin.giraph.process.computer.util;

import com.tinkerpop.gremlin.giraph.Constants;
import com.tinkerpop.gremlin.giraph.hdfs.KeyHelper;
import com.tinkerpop.gremlin.giraph.hdfs.KryoWritableIterator;
import com.tinkerpop.gremlin.giraph.process.computer.GiraphGraphComputer;
import com.tinkerpop.gremlin.giraph.process.computer.GiraphMap;
import com.tinkerpop.gremlin.giraph.process.computer.GiraphReduce;
import com.tinkerpop.gremlin.giraph.structure.GiraphGraph;
import com.tinkerpop.gremlin.process.computer.GraphComputer;
import com.tinkerpop.gremlin.process.computer.MapReduce;
import com.tinkerpop.gremlin.process.computer.SideEffects;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.giraph.io.VertexInputFormat;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;

import java.io.IOException;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class MapReduceHelper {

    private static final String SEQUENCE_WARNING = "The " + Constants.GREMLIN_SIDE_EFFECT_OUTPUT_FORMAT_CLASS
            + " is not " + SequenceFileOutputFormat.class.getCanonicalName()
            + " and thus, sideEffects can not be converted to Java objects";

    public static void executeMapReduceJob(final MapReduce mapReduce, final SideEffects sideEffects, final Configuration configuration) throws IOException, ClassNotFoundException, InterruptedException {
        final Configuration newConfiguration = new Configuration(configuration);
        final org.apache.commons.configuration.Configuration apacheConfiguration = new BaseConfiguration();
        mapReduce.storeState(apacheConfiguration);
        ConfUtil.mergeApacheIntoHadoopConfiguration(apacheConfiguration, newConfiguration);
        if (!mapReduce.doStage(MapReduce.Stage.MAP)) {
            final Path sideEffectPath = new Path(configuration.get(Constants.GREMLIN_OUTPUT_LOCATION) + "/" + KeyHelper.makeDirectory(mapReduce.getSideEffectKey()));
            if (newConfiguration.getClass(Constants.GREMLIN_SIDE_EFFECT_OUTPUT_FORMAT_CLASS, SequenceFileOutputFormat.class, OutputFormat.class).equals(SequenceFileOutputFormat.class))
                mapReduce.addToSideEffects(sideEffects, new KryoWritableIterator(configuration, sideEffectPath));
            else
                GiraphGraphComputer.LOGGER.warn(SEQUENCE_WARNING);
        } else {
            newConfiguration.setClass(Constants.MAP_REDUCE_CLASS, mapReduce.getClass(), MapReduce.class);
            final Job job = new Job(newConfiguration, mapReduce.toString());
            GiraphGraphComputer.LOGGER.info(Constants.GIRAPH_GREMLIN_JOB_PREFIX + mapReduce.toString());
            job.setJarByClass(GiraphGraph.class);
            job.setMapperClass(GiraphMap.class);
            if (mapReduce.doStage(MapReduce.Stage.COMBINE))
                job.setCombinerClass(GiraphReduce.class);
            if (mapReduce.doStage(MapReduce.Stage.REDUCE))
                job.setReducerClass(GiraphReduce.class);
            else
                job.setNumReduceTasks(0);
            job.setMapOutputKeyClass(KryoWritable.class);
            job.setMapOutputValueClass(KryoWritable.class);
            job.setOutputKeyClass(KryoWritable.class);
            job.setOutputValueClass(KryoWritable.class);
            job.setInputFormatClass(ConfUtil.getInputFormatFromVertexInputFormat((Class) newConfiguration.getClass(Constants.GIRAPH_VERTEX_INPUT_FORMAT_CLASS, VertexInputFormat.class)));
            job.setOutputFormatClass(newConfiguration.getClass(Constants.GREMLIN_SIDE_EFFECT_OUTPUT_FORMAT_CLASS, SequenceFileOutputFormat.class, OutputFormat.class)); // TODO: Make this configurable
            // if there is no vertex program, then grab the graph from the input location
            final Path graphPath = configuration.get(GraphComputer.VERTEX_PROGRAM, null) != null ?
                    new Path(newConfiguration.get(Constants.GREMLIN_OUTPUT_LOCATION) + "/" + Constants.TILDA_G) :
                    new Path(newConfiguration.get(Constants.GREMLIN_INPUT_LOCATION));
            final Path sideEffectPath = new Path(newConfiguration.get(Constants.GREMLIN_OUTPUT_LOCATION) + "/" + KeyHelper.makeDirectory(mapReduce.getSideEffectKey()));
            FileInputFormat.setInputPaths(job, graphPath);
            FileOutputFormat.setOutputPath(job, sideEffectPath);
            job.waitForCompletion(true);
            // if its not a SequenceFile there is no certain way to convert to necessary Java objects.
            // to get results you have to look through HDFS directory structure. Oh the horror.
            if (newConfiguration.getClass(Constants.GREMLIN_SIDE_EFFECT_OUTPUT_FORMAT_CLASS, SequenceFileOutputFormat.class, OutputFormat.class).equals(SequenceFileOutputFormat.class))
                mapReduce.addToSideEffects(sideEffects, new KryoWritableIterator(configuration, sideEffectPath));
            else
                GiraphGraphComputer.LOGGER.warn(SEQUENCE_WARNING);
        }
    }
}

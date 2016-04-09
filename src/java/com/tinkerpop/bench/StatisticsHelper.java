package com.tinkerpop.bench;

import java.util.Random;

import com.tinkerpop.bench.evaluators.Evaluator;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import cz.cuni.mff.bluebench.utils.GraphUtils;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author Alex Averbuch (alex.averbuch@gmail.com)
 * @author Martin Neumann (m.neumann.1980@gmail.com)
 */
public class StatisticsHelper {

        private static Random rand = new Random(42);
        private static long time = -1l;
        private static long memory = -1l;
        
        public static int STOPWATCH_START = 1;
        public static int STOPWATCH_STOP = 2;

        public static Vertex[] getSampleVertexIds(Graph db, Evaluator evaluator,
                int sampleSize) {

                Vertex[] samples = new Vertex[sampleSize];
                Double[] sampleVals = new Double[sampleSize];

                double totalVal = evaluator.evaluateTotal(db);

                for (int i = 0; i < sampleVals.length; i++) {
                        sampleVals[i] = rand.nextDouble() * totalVal;
                        samples[i] = null;
                }

                boolean finished = true;

                for (Vertex currentVertex : GraphUtils.getVertexCollection(db)) {

                        double currentVal = evaluator.evaluate(currentVertex);

                        finished = true;

                        for (int i = 0; i < sampleVals.length; i++) {
                                if (samples[i] == null) {
                                        sampleVals[i] -= currentVal;
                                        if (sampleVals[i] <= 0) {
                                                samples[i] = currentVertex;
                                        } else {
                                                finished = false;
                                        }
                                }
                        }

                        if (finished == true) {
                                break;
                        }
                }

                return samples;
        }

        public static long stopWatch(int phase) {
                if (phase == StatisticsHelper.STOPWATCH_START) {
                        time = System.currentTimeMillis();
                        return time;
                } else {
                        long temp = System.currentTimeMillis() - time;
                        time = -1l;
                        return temp;
                }
        }

        /**
         * A "stopwatch" for memory usage
         *
         * @return the memory usage
         */
        public static long stopMemoryWatch(int phase) {
                final Runtime rt = Runtime.getRuntime();
                if (phase == StatisticsHelper.STOPWATCH_START) {
                        memory = rt.totalMemory() - rt.freeMemory();
                        return memory;
                } else {
                        long temp = rt.totalMemory() - rt.freeMemory() - memory;
                        memory = 1l;
                        return temp;
                }
        }
}

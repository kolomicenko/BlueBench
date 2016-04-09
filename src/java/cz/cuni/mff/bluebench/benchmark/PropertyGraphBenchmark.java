package cz.cuni.mff.bluebench.benchmark;

import com.tinkerpop.bench.Bench;
import com.tinkerpop.bench.benchmark.Benchmark;
import com.tinkerpop.bench.operation.Operation;
import com.tinkerpop.bench.operation.DeleteGraph;
import com.tinkerpop.bench.operationFactory.OperationFactory;
import com.tinkerpop.bench.operationFactory.OperationFactoryGeneric;
import cz.cuni.mff.bluebench.DB;
import cz.cuni.mff.bluebench.factories.OperationFactoryMultiple;
import cz.cuni.mff.bluebench.factories.OperationFactorySimple;
import cz.cuni.mff.bluebench.operation.RandomColorArgProvider;
import cz.cuni.mff.bluebench.operation.RandomNameArgProvider;
import cz.cuni.mff.bluebench.operation.VertexIdsArgProvider;
import cz.cuni.mff.bluebench.operations.Dijkstra;
import cz.cuni.mff.bluebench.operations.FindEdgesByProperty;
import cz.cuni.mff.bluebench.operations.FindNeighbours;
import cz.cuni.mff.bluebench.operations.FindVerticesByProperty;
import cz.cuni.mff.bluebench.operations.LoadGraphML;
import cz.cuni.mff.bluebench.operations.RemoveVertices;
import cz.cuni.mff.bluebench.operations.ShortestPath;
import cz.cuni.mff.bluebench.operations.ShortestPathLabeled;
import cz.cuni.mff.bluebench.operations.Traversal;
import cz.cuni.mff.bluebench.operations.TraversalNative;
import cz.cuni.mff.bluebench.operations.UpdateProperties;
import java.util.ArrayList;
import java.util.List;

/**
 * Labeled Graph Benchmark definition.
 * 
 * @author Sidney Shaw
 */
public class PropertyGraphBenchmark extends Benchmark {
        private static final String name = "PropertyGraphBenchmark";
        private static final List<DB> testedDBs;
        private static final List<String> graphmlFilenames;

        static {
                testedDBs = new ArrayList<>();
                testedDBs.add(DB.DEX);
                testedDBs.add(DB.Infinite);
                testedDBs.add(DB.Mysql);
                testedDBs.add(DB.Orient);
                testedDBs.add(DB.Neo4j);
                testedDBs.add(DB.Mongo);
                testedDBs.add(DB.Titan);
                testedDBs.add(DB.Tinker);
                
                graphmlFilenames = new ArrayList<>();
                
//                graphmlFilenames.add("100x5_with_properties.graphml");
                graphmlFilenames.add("1000x5_with_properties.graphml");
                graphmlFilenames.add("50000x5_with_properties.graphml");
                graphmlFilenames.add("100000x5_with_properties.graphml");
                graphmlFilenames.add("200000x5_with_properties.graphml");
                
//                graphmlFilenames.add("100x10_with_properties.graphml");
                graphmlFilenames.add("1000x10_with_properties.graphml");
                graphmlFilenames.add("50000x10_with_properties.graphml");
                graphmlFilenames.add("100000x10_with_properties.graphml");
                graphmlFilenames.add("200000x10_with_properties.graphml");
        }
        
        
        public PropertyGraphBenchmark() {
                super(DB.Dummy);
        }
        
        /**
         * Runs the benchmark.
         * 
         * @throws Exception 
         */
        public static void run() throws Exception {
                new PropertyGraphBenchmark();
        }
        
        /**
         * Returns the factories and their operations.
         * 
         * @return 
         */
        @Override
        protected ArrayList<OperationFactory> getOperationFactories() {
                ArrayList<OperationFactory> factories = new ArrayList<>();

                for (String graphmlFilename : this.getInputFiles()) {
                        
                        String operationTag = Operation.createTag(graphmlFilename);

                        factories.add(new OperationFactoryGeneric(
                                DeleteGraph.class, 1));

                        factories.add(new OperationFactoryGeneric(
                                LoadGraphML.class, 1,
                                new String[]{graphmlFilename}, operationTag));

                        factories.add(
                                new OperationFactoryMultiple(
                                new Class[]{Dijkstra.class},
                                Bench.REPETITIONS, operationTag, new VertexIdsArgProvider(1)));

                        factories.add(
                                new OperationFactoryMultiple(
                                new Class[]{TraversalNative.class, Traversal.class},
                                Bench.REPETITIONS, operationTag, new VertexIdsArgProvider(1)));

                        // same as previous but switched order
                        factories.add(
                                new OperationFactoryMultiple(
                                new Class[]{Traversal.class, TraversalNative.class},
                                Bench.REPETITIONS, operationTag, new VertexIdsArgProvider(1)));
                        
                        factories.add(
                                new OperationFactoryMultiple(
                                new Class[]{ShortestPath.class},
                                Bench.REPETITIONS, operationTag, new VertexIdsArgProvider(2)));
                        
                        factories.add(
                                new OperationFactoryMultiple(
                                new Class[]{ShortestPathLabeled.class},
                                Bench.REPETITIONS, operationTag, new VertexIdsArgProvider(2)));

                        factories.add(new OperationFactorySimple(FindNeighbours.class, Bench.REPETITIONS, operationTag));

                        factories.add(new OperationFactoryMultiple(
                                new Class[]{FindEdgesByProperty.class},
                                Bench.REPETITIONS, operationTag, new RandomColorArgProvider()));
                        
                        factories.add(new OperationFactoryMultiple(
                                new Class[]{FindVerticesByProperty.class},
                                Bench.REPETITIONS, operationTag, new RandomNameArgProvider()));
                        
                        factories.add(
                                new OperationFactoryMultiple(
                                new Class[]{UpdateProperties.class},
                                Bench.REPETITIONS, operationTag, new VertexIdsArgProvider(1)));
                        
//                        factories.add(new OperationFactoryGeneric(
//                                OperationUpdateEdgeProperties.class, Bench.REPETITIONS,
//                                new String[]{}, graphmlFilename));
                        
                        factories.add(
                                new OperationFactoryMultiple(
                                new Class[]{RemoveVertices.class},
                                Bench.REPETITIONS, operationTag, new VertexIdsArgProvider(1)));
                        
                }


                return factories;
        }

        /**
         * Returns the name of the benchmark.
         * 
         * @return 
         */
        @Override
        protected String getName() {
                return name;
        }

        /**
         * Returns a List of all the tested GDBs.
         * 
         * @return 
         */
        @Override
        protected List<DB> getTestedDbs() {
                return testedDBs;
        }
        
        /**
         * Returns the list of the tested graph input files.
         * 
         * @return 
         */
        @Override
        protected List<String> getInputFiles() {
                return graphmlFilenames;
        }
}

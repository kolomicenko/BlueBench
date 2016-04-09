package cz.cuni.mff.bluebench.operations;

import com.infinitegraph.AccessMode;
import com.infinitegraph.ConfigurationException;
import com.infinitegraph.GraphDatabase;
import com.infinitegraph.GraphFactory;
import com.infinitegraph.StorageException;
import com.infinitegraph.Transaction;
import com.tinkerpop.bench.Bench;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.impls.dex.DexGraph;
import cz.cuni.mff.bluebench.operation.NativeOperation;

import com.infinitegraph.indexing.*;
import com.tinkerpop.blueprints.Vertex;
import cz.cuni.mff.bluebench.DB;
import cz.cuni.mff.bluebench.utils.IGEdgeWithProperties;
import cz.cuni.mff.bluebench.utils.IGVertexWithProperties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Operation which creates indexes in the graph.
 * 
 * @author Sidney Shaw
 */
public class CreateIndexes extends NativeOperation {

        protected String weightProp;
        protected String colorProp;
        
        protected String ageProp;
        protected String nameProp;
        
        protected KeyIndexableGraph graph;
        
        @Override
        public void initNative(String[] args) {
                this.weightProp = Bench.benchProperties.getProperty(Bench.GRAPH_PROPERTY_WEIGHT);
                this.colorProp = Bench.benchProperties.getProperty(Bench.GRAPH_PROPERTY_COLOR);
                this.ageProp = Bench.benchProperties.getProperty(Bench.GRAPH_PROPERTY_AGE);
                this.nameProp = Bench.benchProperties.getProperty(Bench.GRAPH_PROPERTY_NAME);
                
                this.graph = (KeyIndexableGraph) this.getGraph();
        }
        
        @Override
        public Object execNative() {
                graph.createKeyIndex(weightProp, Edge.class);
                graph.createKeyIndex(colorProp, Edge.class);
                
                graph.createKeyIndex(ageProp, Vertex.class);
                graph.createKeyIndex(nameProp, Vertex.class);
                
                return "DONE";
        }
        
        @Override
        protected NativeOperation getNativeDexImpl() {
                return new CreateIndexes.Dex();
        }
        
        private class Dex extends CreateIndexes {
                
                @Override
                public Object execNative() {
                        DexGraph dexGraph = (DexGraph) this.getGraph();
                        
                        // in order to create an index, graph's label must be set
                        dexGraph.label.set("bluebenchEdge");
                        
                        graph.createKeyIndex(weightProp, Edge.class);
                        graph.createKeyIndex(colorProp, Edge.class);
                        
                        // in order to create an index, graph's label must be set
                        dexGraph.label.set("bluebenchVertex");
                        
                        graph.createKeyIndex(ageProp, Vertex.class);
                        graph.createKeyIndex(nameProp, Vertex.class);
                        
                        return "DONE";
                }
        }
        
        @Override
        protected NativeOperation getNativeTitanImpl() {
                return new CreateIndexes.Titan();
        }
        
        private class Titan extends CreateIndexes {
                
                @Override
                public Object execNative() {
                        // Titan doesn't allow its edges to be indexed
                        graph.createKeyIndex(ageProp, Vertex.class);
                        graph.createKeyIndex(nameProp, Vertex.class);

                        return "DONE";
                }
        }
        
        @Override
        protected NativeOperation getNativeIGImpl() {
                return new CreateIndexes.IG();
        }
        
        private class IG extends CreateIndexes {
                
                private GraphDatabase nativeDb = null;
                private Transaction tx = null;
                
                @Override
                public void initNative(String[] args) {
                        this.weightProp = Bench.benchProperties.getProperty(Bench.GRAPH_PROPERTY_WEIGHT);
                        this.colorProp = Bench.benchProperties.getProperty(Bench.GRAPH_PROPERTY_COLOR);
                        this.ageProp = Bench.benchProperties.getProperty(Bench.GRAPH_PROPERTY_AGE);
                        this.nameProp = Bench.benchProperties.getProperty(Bench.GRAPH_PROPERTY_NAME);
                        
                        this.getGraph().shutdown();
                        
                        try {
                                nativeDb = GraphFactory.open(
                                        Bench.benchProperties.getProperty(Bench.IG_DB_NAME), 
                                        DB.Infinite.cfgPath
                                );
                                
                        } catch (ConfigurationException | StorageException ex) {
                                Logger.getLogger(LoadGraphML.class.getName()).log(Level.SEVERE, null, ex);
                        }
                }
                
                @Override
                public Object execNative() {
                        
                        tx = nativeDb.beginTransaction(AccessMode.READ_WRITE);
                        
                        try {
                                // even though indexes are dropped long time ago, there's a bug causing the impossibility
                                // to create an index with a name any index has had before, hence the time
                                IndexManager.addGraphIndex(
                                        new Long(System.currentTimeMillis()).toString(), 
                                        IGEdgeWithProperties.class.getName(), new String[] {weightProp}, false
                                );
                                IndexManager.addGraphIndex(
                                        new Long(System.currentTimeMillis()).toString(), 
                                        IGEdgeWithProperties.class.getName(), new String[] {colorProp}, false
                                );
                                IndexManager.addGraphIndex(
                                        new Long(System.currentTimeMillis()).toString(), 
                                        IGVertexWithProperties.class.getName(), new String[] {ageProp}, false
                                );
                                IndexManager.addGraphIndex(
                                        new Long(System.currentTimeMillis()).toString(), 
                                        IGVertexWithProperties.class.getName(), new String[] {nameProp}, false
                                );
                        } catch (IndexException ex) {
                                Logger.getLogger(CreateIndexes.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        
                        tx.commit();
                        tx.complete();
                        
                        return "DONE";
                }
                
                @Override
                public void concludeNative() {
                        nativeDb.close();
                        
                        try {
                                this.getGraphDescriptor().openGraph();
                        } catch (Exception ex) {
                                Logger.getLogger(LoadGraphML.class.getName()).log(Level.SEVERE, null, ex);
                        }
                }
        }

}

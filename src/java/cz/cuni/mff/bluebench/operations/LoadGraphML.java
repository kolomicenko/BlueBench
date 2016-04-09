package cz.cuni.mff.bluebench.operations;

import com.infinitegraph.*;
import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.bench.Bench;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import cz.cuni.mff.bluebench.DB;
import cz.cuni.mff.bluebench.operation.GraphFiller;
import cz.cuni.mff.bluebench.operation.NativeOperation;
import cz.cuni.mff.bluebench.utils.IGEdge;
import cz.cuni.mff.bluebench.utils.IGEdgeWithProperties;
import cz.cuni.mff.bluebench.utils.IGVertex;
import cz.cuni.mff.bluebench.utils.IGVertexWithProperties;
import cz.cuni.mff.bluebench.utils.GraphMLReader;
import cz.cuni.mff.bluebench.utils.GraphUtils;
import java.io.FileInputStream;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Loads the graph database with a provided GraphML file.
 * 
 * @author Sidney Shaw
 */
public class LoadGraphML extends NativeOperation implements GraphFiller{

	protected String graphmlPath = null;
	private final int TRANSACTION_BUFFER = 1000;

	// args
	// -> 0 graphmlDir
	@Override
	public void initNative(String[] args) {
		this.graphmlPath = Bench.benchProperties.getProperty(Bench.DATASETS_DIRECTORY) + args[0];
	}

	@Override
	public Object execNative() {
		try {
			Map<Integer, Object> map = GraphMLReader.inputGraph(new FileInputStream(graphmlPath), this, 
                                getTransactionBufferSize(), "v__id", "e__id", "e__label");
                        
                        GraphUtils.setIdMapping(map);
		} catch (Exception e) {
			e.printStackTrace();
		}
                
                return "DONE";
	}
        
        
        @Override
        public Object addVertex(Object vertexId, Map<String, Object> vertexProps) {
                Vertex result = this.getGraph().addVertex(vertexId);
                
                for (Map.Entry<String, Object> prop : vertexProps.entrySet()) {
                        result.setProperty(prop.getKey(), prop.getValue());
                }
                
                return result;
        }

        @Override
        public Object resolveVertexId(Object vertex) {
                return ((Vertex) vertex).getId();
        }

        @Override
        public void addEdge(Object edgeId, Object source, Object target, String label, Map<String, Object> edgeProps) {
                Vertex sourceVertex = (Vertex) source;
                Vertex targetVertex = (Vertex) target;
                Graph graph = this.getGraph();
                
                if (graph instanceof TitanGraph) {
                        // we have to refresh the vertices in case they weren't
                        // saved in the current transaction
                        sourceVertex = graph.getVertex(sourceVertex);
                        targetVertex = graph.getVertex(targetVertex);
                }
                
                Edge edge = graph.addEdge(edgeId, sourceVertex, targetVertex, label);
                
                for (Map.Entry<String, Object> prop : edgeProps.entrySet()) {
                        edge.setProperty(prop.getKey(), prop.getValue());
                }
        }
        
        @Override
        public void commit(){
                GraphUtils.commit(this.getGraph());
        }
        
        protected int getTransactionBufferSize() {
                return TRANSACTION_BUFFER;
        }
        
//        //changing the DEX's "label" before any element is added for the indexing to work
//        //however, this makes the loading time terribly long (so whether the indexes really work wasn't verified)
//        @Override
//        protected NativeOperation getNativeDexImpl() {
//                return new Dex();
//        }
//        
//        private class Dex extends LoadGraphML {
//                private DexGraph dexGraph;
//                
//                @Override
//                public void initNative(String[] args) {
//                        super.initNative(args);
//                        
//                        dexGraph = (DexGraph) this.getGraph();
//                }
//                
//                @Override
//                public Object addVertex(Object vertexId, Map<String, Object> vertexProps) {
//                        dexGraph.label.set("v" + vertexId);
//                        
//                        return super.addVertex(vertexId, vertexProps);
//                }
//                
//                @Override
//                public void addEdge(Object edgeId, Object source, Object target, String label, Map<String, Object> edgeProps) {
//                        dexGraph.label.set("e" + edgeId);
//                        
//                        super.addEdge(edgeId, source, target, label, edgeProps);
//                }
//        }
        
        @Override
        protected NativeOperation getNativeOrientImpl() {
                return new Orient();
        }
        
        private class Orient extends LoadGraphML {
                // Orient isn't capable of having a larger buffer
                private final int TRANSACTION_BUFFER = 100;
                
                @Override
                protected int getTransactionBufferSize() {
                        return TRANSACTION_BUFFER;
                }
        }
        
        @Override
        protected NativeOperation getNativeIGImpl() {
                return new IG(this);
        }

        private class IG extends LoadGraphML {
                
                public IG(LoadGraphML parent) {
                        this.parent = parent;
                }
                
                private LoadGraphML parent = null;
                
                private GraphDatabase nativeDb = null;
                private Transaction tx = null;
                
                private String weightProp;
                private String colorProp;
                
                private String ageProp;
                private String nameProp;
                
                @Override
                public void initNative(String[] args) {
                        super.initNative(args);
                        
                        this.weightProp = Bench.benchProperties.getProperty(Bench.GRAPH_PROPERTY_WEIGHT);
                        this.colorProp = Bench.benchProperties.getProperty(Bench.GRAPH_PROPERTY_COLOR);
                        
                        this.ageProp = Bench.benchProperties.getProperty(Bench.GRAPH_PROPERTY_AGE);
                        this.nameProp = Bench.benchProperties.getProperty(Bench.GRAPH_PROPERTY_NAME);
                        
                        this.parent.getGraph().shutdown();
                        try {
                                nativeDb = GraphFactory.open(
                                        Bench.benchProperties.getProperty(Bench.IG_DB_NAME), 
                                        DB.Infinite.cfgPath
                                );
                                
                        } catch (ConfigurationException | StorageException ex) {
                                Logger.getLogger(LoadGraphML.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        
                        tx = nativeDb.beginTransaction(AccessMode.READ_WRITE);
                }
                
                @Override
                public void concludeNative() {
                        tx.complete();
                        nativeDb.close();
                        
                        try {
                                this.parent.getGraphDescriptor().openGraph();
                        } catch (Exception ex) {
                                Logger.getLogger(LoadGraphML.class.getName()).log(Level.SEVERE, null, ex);
                        }
                }
                
                @Override
                public Object addVertex(Object vertexId, Map<String, Object> vertexProps) {
                        IGVertex vertex = null;
                                
                        if (!vertexProps.isEmpty()) {
                                vertex = new IGVertexWithProperties(
                                        Long.valueOf((String) vertexProps.get(ageProp)),
                                        vertexProps.get(nameProp).toString()
                                );
                                
                        } else {
                                vertex = new IGVertex();
                        }        
                                
                        nativeDb.addVertex(vertex);
                        
                        return vertex;
                }
                
                @Override
                public Object resolveVertexId(Object vertex) {
                        return ((IGVertex) vertex).getId();
                }
                
                @Override
                public void addEdge(Object edgeId, Object source, Object target, String label, Map<String, Object> edgeProps) {
                        IGVertex sourceVertex = (IGVertex) source;
                        IGVertex targetVertex = (IGVertex) target;
                        IGEdge edge = null;
                        
                        short weight = 1;
                        if (!edgeProps.isEmpty()) {
                                edge = new IGEdgeWithProperties(
                                        Long.valueOf((String) edgeProps.get(weightProp)),
                                        edgeProps.get(colorProp).toString()
                                );
                                
                                weight = Short.valueOf((String) edgeProps.get(weightProp));
                        } else {
                                edge = new IGEdge();
                        }
                        
                        sourceVertex.addEdge(edge, targetVertex, EdgeKind.OUTGOING, weight);
                        edge.setLabel(label);
                        
                        if (!edgeProps.isEmpty()) {
                                //edge's properties are set automatically in the constructor, but are sometimes ignored
                                edge.setProperty(weightProp, Long.valueOf((String) edgeProps.get(weightProp)));
                                edge.setProperty(colorProp, edgeProps.get(colorProp).toString());
                        }
                }
                
                @Override
                public void commit(){
                        tx.commit();
                        tx = nativeDb.beginTransaction(AccessMode.READ_WRITE);
                }
        }
}

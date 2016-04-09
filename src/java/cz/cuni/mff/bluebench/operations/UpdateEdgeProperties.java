package cz.cuni.mff.bluebench.operations;

import com.tinkerpop.bench.Bench;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import cz.cuni.mff.bluebench.operation.NativeOperation;
import cz.cuni.mff.bluebench.utils.GraphUtils;

/**
 * Updates properties of specified edges.
 * 
 * @author Sidney Shaw
 */
public class UpdateEdgeProperties extends NativeOperation{
        
        protected Graph graph;
        protected final int TRANSACTION_BUFFER = 100;
        protected String colorPropertyName = null;
        
        @Override
	public void initNative(String[] args) {
                this.graph = this.getGraph();
                GraphUtils.commit(graph);
                
                this.colorPropertyName = Bench.benchProperties.getProperty(Bench.GRAPH_PROPERTY_COLOR);
	}

	@Override
	public Object execNative(){
                int count = 0;
                for (Edge e : graph.getEdges()) {
                        this.updateProperty(e);
                        
                        if (++count % TRANSACTION_BUFFER == 0){
                                GraphUtils.commit(graph);
                        }
                }
                
                return count;
	}
        
        protected void updateProperty(Edge e) {
                e.setProperty(colorPropertyName, e.getProperty(colorPropertyName).toString() + "filling");
        }
        
        @Override
        protected NativeOperation getNativeTitanImpl() {
                return new Titan();
        }
        
        private class Titan extends UpdateEdgeProperties {
                @Override
                protected void updateProperty(Edge e) {
                        e = graph.getEdge(e); // refresh the edge (transaction issues)
                        e.setProperty(colorPropertyName, e.getProperty(colorPropertyName).toString() + "filling");
                }
        }
}

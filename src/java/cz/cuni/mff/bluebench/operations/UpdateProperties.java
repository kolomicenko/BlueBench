package cz.cuni.mff.bluebench.operations;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import cz.cuni.mff.bluebench.operation.NativeOperation;
import cz.cuni.mff.bluebench.utils.GraphUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Updates properties of specified vertices.
 * 
 * @author Sidney Shaw
 */
public class UpdateProperties extends NativeOperation{
        
        protected Graph graph;
        private final int maxHops = 2;
        protected final int TRANSACTION_BUFFER = 1000;
        private HashSet<Vertex> verticesToUpdate = new HashSet<>();
        
        @Override
	public void initNative(String[] args) {
                this.graph = this.getGraph();
                GraphUtils.commit(graph);
                
                Vertex startingVertex = GraphUtils.getVertex(Integer.valueOf(args[0]), getGraph());
                
                this.verticesToUpdate = GraphUtils.BFS(startingVertex, maxHops, Direction.BOTH);
	}

	@Override
	public Object execNative(){
                return this.updateVertices();
	}
        
        protected int updateVertices() {
                int count = 0;
                Vertex last = null;
                for (Vertex v : verticesToUpdate) {
                        if (last == null) {
                                last = v;
                        } else {
                                this.switchProperties(v, last);
                                last = null;
                        }
                        
                        if (++count % TRANSACTION_BUFFER == 0){
                                GraphUtils.commit(graph);
                        }
                }
                
                GraphUtils.commit(graph);
                
                return count;
        }
        
        protected void switchProperties(Vertex v, Vertex w) {
                Set<String> keys = v.getPropertyKeys();
                // switch the property values between v and last
                for (String key : keys) {
                        Object val = v.getProperty(key);
                        v.setProperty(key, w.getProperty(key));
                        w.setProperty(key, val);
                }
        }
        
        @Override
        protected NativeOperation getNativeTitanImpl() {
                return new Titan();
        }
        
        private class Titan extends UpdateProperties {
                @Override
                protected void switchProperties(Vertex v, Vertex w) {
                        v = graph.getVertex(v); // refresh the vertex (transaction issues)
                        w = graph.getVertex(w); // refresh the vertex (transaction issues)
                        Set<String> keys = v.getPropertyKeys();
                        // switch the property values between v and last
                        for (String key : keys) {
                                Object val = v.getProperty(key);
                                v.setProperty(key, w.getProperty(key));
                                w.setProperty(key, val);
                        }
                }
        }
}

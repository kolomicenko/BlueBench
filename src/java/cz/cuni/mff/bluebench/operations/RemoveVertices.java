package cz.cuni.mff.bluebench.operations;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import cz.cuni.mff.bluebench.operation.NativeOperation;
import cz.cuni.mff.bluebench.utils.GraphUtils;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Removes a number of vertices from the graph.
 * 
 * @author Sidney Shaw
 */
public class RemoveVertices extends NativeOperation{
        
        private HashSet<Vertex> verticesToDelete = new HashSet<>();
        protected Graph graph;
        private final int maxHops = 2;
        private final int TRANSACTION_BUFFER = 1000;
        
        @Override
	public void initNative(String[] args) {
                this.graph = this.getGraph();
                GraphUtils.commit(graph);
                
                Vertex startingVertex = GraphUtils.getVertex(Integer.valueOf(args[0]), getGraph());
                
                if (startingVertex == null) {
                        // the vertex's been already deleted
                        return;
                }
                
                this.verticesToDelete = GraphUtils.BFS(startingVertex, maxHops, Direction.BOTH);
	}

	@Override
	public Object execNative(){
                
                return this.deleteVertices();
	}
        
        protected int deleteVertices() {
                int bufferSize = this.getTransactionBufferSize();
                int count = 0;
                for (Vertex v : verticesToDelete) {
                        this.deleteVertex(v);
                        if (++count % bufferSize == 0){
                                GraphUtils.commit(graph);
                        }
                }
                
                GraphUtils.commit(graph);
                
                return count;
        }
        
        protected void deleteVertex(Vertex v) {
                graph.removeVertex(v);
        }
        
        protected int getTransactionBufferSize() {
                return this.TRANSACTION_BUFFER;
        }
        
        @Override
        protected NativeOperation getNativeTitanImpl() {
                return new Titan();
        }
        
        private class Titan extends RemoveVertices {
                @Override
                protected void deleteVertex(Vertex v) {
                        graph.removeVertex(graph.getVertex(v)); // refresh the vertex (transaction issues)
                }
        }
        
        @Override
        protected NativeOperation getNativeOrientImpl() {
                return new Orient();
        }
        
        private class Orient extends RemoveVertices {
                private final int TRANSACTION_BUFFER = 100;
                
                @Override
                protected void deleteVertex(Vertex v) {
                        graph.removeVertex(graph.getVertex(v.getId())); // refresh the vertex (transaction issues)
                }
                
                @Override
                protected int getTransactionBufferSize() {
                        return this.TRANSACTION_BUFFER;
                }
        }
}

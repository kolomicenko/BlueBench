package cz.cuni.mff.bluebench.operations;

import com.tinkerpop.bench.operation.Operation;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import cz.cuni.mff.bluebench.utils.GraphUtils;

/**
 * Operation which gets the neighbors of a specified vertex.
 * 
 * @author Sidney Shaw
 */
public class FindNeighbours extends Operation{
        
        private Vertex startingVertex = null;
        
        @Override
	protected void onInitialize(String[] args) {
                this.startingVertex = GraphUtils.getVertex(Integer.valueOf(args[0]), getGraph());
	}

	@Override
	protected void onExecute() throws Exception {
		try {
			Iterable<Vertex> neighbours = startingVertex.getVertices(Direction.BOTH);
                        
                        int count = 0;
                        for (Vertex v: neighbours) {
                                count++;    
                        }
                        
			setResult(count);
		} catch (Exception e) {
			throw e;
		}
	}
}

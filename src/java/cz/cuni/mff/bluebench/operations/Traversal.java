package cz.cuni.mff.bluebench.operations;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import cz.cuni.mff.bluebench.operation.NativeOperation;
import cz.cuni.mff.bluebench.utils.GraphUtils;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Performs a BFS traversal from a provided vertex.
 * 
 * @author Sidney Shaw
 */
public class Traversal extends NativeOperation {
        
        private Vertex startingVertex = null;
        private String[] labels = new String[]{};
        private Direction direction = Direction.OUT;
        protected int maxHops = 5;
        
        @Override
	public void initNative(String[] args) {
                this.startingVertex = GraphUtils.getVertex(Integer.valueOf(args[0]), getGraph());
	}

	@Override
	public Object execNative() {
                HashSet<Vertex> result = GraphUtils.BFS(startingVertex, maxHops, direction, labels);
                
                return result.size();
	}
}

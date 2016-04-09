package cz.cuni.mff.bluebench.operations;

import com.tinkerpop.bench.Bench;
import com.tinkerpop.bench.operation.Operation;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.furnace.algorithms.search.BreadthFirstSearch;
import com.tinkerpop.furnace.algorithms.search.SearchAlgorithm;
import cz.cuni.mff.bluebench.utils.GraphUtils;
import java.util.List;

/**
 * Performs a labeled shortest path search between two specified vertices.
 * 
 * @author Sidney Shaw
 */
public class ShortestPathLabeled extends Operation{
        
        private Vertex startingVertex = null;
        private Vertex targetVertex = null;
        private SearchAlgorithm algorithm = null;
        private String[] labels = new String[]{Bench.benchProperties.getProperty(Bench.GRAPH_LABEL_FRIEND)};
        
        @Override
	public void onInitialize(String[] args) {
                this.algorithm = new BreadthFirstSearch(getGraph());
                
                this.startingVertex = GraphUtils.getVertex(args[0], getGraph());
                this.targetVertex = GraphUtils.getVertex(args[1], getGraph());
	}

	@Override
	protected void onExecute() throws Exception {
                List<Edge> path = algorithm.findPathToTarget(startingVertex, targetVertex, labels);
                List<Edge> pathReversed = algorithm.findPathToTarget(targetVertex, startingVertex, labels);
                
                if (path == null) {
                        if (pathReversed == null) {
                                setResult(-1);
                        } else {
                                setResult(pathReversed.size());
                        }
                } else {
                        if (pathReversed == null) {
                                setResult(path.size());
                        } else {
                                setResult(Math.min(path.size(), pathReversed.size()));
                        } 
                }
	}

}

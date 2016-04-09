package cz.cuni.mff.bluebench.operations;

import com.tinkerpop.bench.Bench;
import com.tinkerpop.bench.operation.Operation;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.furnace.algorithms.shortestpath.DijkstrasAlgorithm;
import com.tinkerpop.furnace.algorithms.shortestpath.SingleSourceShortestPathAlgorithm;
import cz.cuni.mff.bluebench.utils.GraphUtils;
import java.util.List;
import java.util.Map;

/**
 * Performs Dijkstra's algorithm on the graph.
 * 
 * @author Sidney Shaw
 */
public class Dijkstra extends Operation{
        
        private Vertex startingVertex = null;
        private SingleSourceShortestPathAlgorithm algorithm = null;
        private String weightPropertyName = null;
        private String[] labels = null;
        
        protected Vertex getVertexByProperty(String propertyName, String propertyValue) {
                return this.getGraph().getVertices(propertyName, propertyValue).iterator().next();
        }
        
        @Override
	public void onInitialize(String[] args) {
                this.algorithm = new DijkstrasAlgorithm(getGraph());
                
                this.startingVertex = GraphUtils.getVertex(args[0], getGraph());
                this.weightPropertyName = Bench.benchProperties.getProperty(Bench.GRAPH_PROPERTY_WEIGHT);
                this.labels = new String[]{Bench.benchProperties.getProperty(Bench.GRAPH_LABEL_FAMILY)};
	}

	@Override
	protected void onExecute() throws Exception {
                Map<Vertex, List<Edge>> paths = algorithm.compute(startingVertex, weightPropertyName, labels);
                
                int totalLength = 0;
                for (List<Edge> path : paths.values()) {
                        totalLength += path.size();
                }
                
                setResult(totalLength);
	}

}

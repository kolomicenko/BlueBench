
package cz.cuni.mff.bluebench.operation;

import java.util.Map;

/**
 * Defines a class which contains methods for filling the graph.
 *
 * @author Sidney Shaw
 */
public interface GraphFiller {
        public Object addVertex(Object vertexId, Map<String, Object> vertexProps);
        
        public Object resolveVertexId(Object vertex);
        
        public void addEdge(Object edgeId, Object source, Object target, String label, Map<String, Object> edgeProps);
        
        public void commit();
}

package cz.cuni.mff.bluebench.utils;

import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.ig.IGGraph;
import com.tinkerpop.blueprints.impls.sail.impls.NativeStoreSailGraph;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Static utilities methods.
 * 
 * @author Sidney Shaw
 */
public class GraphUtils {
        /**
	 * Close an Iterable if it is CloseableIterable
	 * 
	 * @param iterable the Iterable object
	 */
	public static void close(Iterable<?> iterable) {
		if (iterable instanceof CloseableIterable<?>) {
			((CloseableIterable<?>) iterable).close();
		}
	}
        
        public static void commit(Graph g) {
                if (g instanceof TransactionalGraph) {
                        ((TransactionalGraph) g).stopTransaction(TransactionalGraph.Conclusion.SUCCESS);
                        
                        if (g instanceof IGGraph) {
                                ((IGGraph) g).startTransaction();
                        }
                }
        }
        
        private static Map<String, Vertex> vertexMapping;
        
        private static Map<Integer, Object> idMapping;
        
        public static void setVertexMapping(Map<String, Vertex> vertexMapping, Graph graph) {
                GraphUtils.vertexMapping = vertexMapping;        
        }
        
        public static void setIdMapping(Map<Integer, Object> idMapping) {
                GraphUtils.idMapping = idMapping;
        }
        
        public static List<Integer> getSampleIds(int count) {
                List<Integer> ids = new ArrayList<>(idMapping.keySet());
                Collections.shuffle(ids);
                
                return ids.subList(0, Math.min(ids.size(), count));
        }
        
        public static Vertex getVertex(Integer id, Graph graph) {
//                return vertexMapping.get(id); // for a very strange reason was causing an exception later on
                return graph.getVertex(idMapping.get(id));
        }
        
        public static Vertex getVertex(String id, Graph graph) {
//                return vertexMapping.get(id); // for a very strange reason was causing an exception later on
                return getVertex(Integer.valueOf(id), graph);
        }
        
        public static Object getRawId(Integer id) {
                return idMapping.get(id);
        }
        
        public static Object getRawId(String id) {
                return getRawId(Integer.valueOf(id));
        }
        
        public static Iterable<Vertex> getVertexCollection(Graph graph) {
                if (graph instanceof NativeStoreSailGraph) {
                        return vertexMapping.values();
                } else {
                        return graph.getVertices();
                }
        }
        
        public static HashSet<Vertex> BFS(Vertex startingVertex, int maxHops, Direction direction, String... labels) {
                HashSet<Vertex> result = new HashSet<>();
		ArrayList<Vertex> curr = new ArrayList<>();
		ArrayList<Vertex> next = new ArrayList<>();

		curr.add(startingVertex);

		for (int depth = 0; depth < maxHops; depth++) {

			for (Vertex u : curr) {

				Iterable<Vertex> vi = u.getVertices(direction, labels);
				for (Vertex v : vi) {
					if (result.add(v)) {
						next.add(v);
					}
				}
				GraphUtils.close(vi);
			}

			if (next.isEmpty()) {
				break;
                        }

                        curr = next;
                        next = new ArrayList<>();
		}
                
                return result;
        }
}

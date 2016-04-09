package cz.cuni.mff.bluebench.dummyGraph;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Features;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.Vertex;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Sidney Shaw
 */
public class DummyGraph implements Graph, KeyIndexableGraph {
        
        private static final Features FEATURES = new Features();

        static {
                FEATURES.ignoresSuppliedIds = true;
        }
        
        @Override
        public Features getFeatures(){
                return FEATURES;
        }

        @Override
        public Vertex addVertex(Object o) {
                return new DummyVertex(o);
        }

        @Override
        public Vertex getVertex(Object o) {
                return new DummyVertex(o);
        }

        @Override
        public void removeVertex(Vertex vertex) {
        }

        @Override
        public Iterable<Vertex> getVertices() {
                return new DummyIterator<Vertex>();
        }

        @Override
        public Iterable<Vertex> getVertices(String string, Object o) {
                return new DummyIterator<Vertex>();
        }

        @Override
        public Edge addEdge(Object o, Vertex vertex, Vertex vertex1, String string) {
                return new DummyEdge(o);
        }

        @Override
        public Edge getEdge(Object o) {
                return new DummyEdge(o);
        }

        @Override
        public void removeEdge(Edge edge) {}

        @Override
        public void shutdown() {
        }
        
        @Override
        public Iterable<Edge> getEdges() {
                return new DummyIterator<Edge>();
        }
        
        @Override
        public Iterable<Edge> getEdges(String s, Object o) {
                return new DummyIterator<Edge>();
        }

        @Override
        public <T extends Element> void dropKeyIndex(String string, Class<T> type) {
        }

        @Override
        public <T extends Element> void createKeyIndex(String string, Class<T> type) {
        }

        @Override
        public <T extends Element> Set<String> getIndexedKeys(Class<T> type) {
                return new HashSet<>();
        }
        
}

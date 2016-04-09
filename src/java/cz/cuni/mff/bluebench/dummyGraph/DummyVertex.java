package cz.cuni.mff.bluebench.dummyGraph;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Query;
import com.tinkerpop.blueprints.Vertex;
import java.util.Set;

/**
 *
 * @author Sidney Shaw
 */
public class DummyVertex implements Vertex {
        private Object _id;
        
        public DummyVertex(Object id) {
                this._id = id;
        }
        
        @Override
        public Object getProperty(String string) {
                return null;
        }

        @Override
        public Set<String> getPropertyKeys() {
                return null;
        }

        @Override
        public void setProperty(String string, Object o) {
        }

        @Override
        public Object removeProperty(String string) {
                return null;
        }

        @Override
        public Object getId() {
                return this._id;
        }
        
        @Override
        public Iterable<Edge> getEdges(Direction drctn, String[] strings) {
                return new DummyIterator<Edge>();
        }

        @Override
        public Iterable<Vertex> getVertices(Direction drctn, String[] strings) {
                return new DummyIterator<Vertex>();
        }

        @Override
        public Query query() {
                return null;
        }

}

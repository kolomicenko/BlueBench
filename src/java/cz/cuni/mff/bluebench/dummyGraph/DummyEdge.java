package cz.cuni.mff.bluebench.dummyGraph;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import java.util.Set;

/**
 *
 * @author Sidney Shaw
 */
public class DummyEdge implements Edge{
        private Object _id;
        
        public DummyEdge(Object id) {
                this._id = id;
        }
        
        @Override
        public Vertex getVertex(Direction drctn) throws IllegalArgumentException {
                return null;
        }

        @Override
        public String getLabel() {
                return "";
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

}

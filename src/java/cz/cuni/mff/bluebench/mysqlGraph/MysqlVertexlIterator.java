package cz.cuni.mff.bluebench.mysqlGraph;

import com.tinkerpop.blueprints.Vertex;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helper iterator for MysqlGraph vertices.
 * 
 * @author Sidney Shaw
 */
public class MysqlVertexlIterator implements Iterable, Iterator {
        private ResultSet _resultSet = null;
        private Vertex _nextVertex = null;
        
        private MysqlGraph _graph;
        
        public MysqlVertexlIterator(ResultSet rs, MysqlGraph g) {
                this._resultSet = rs;
                
                this._graph = g;
                
                this.moveForwards();
        }
        
        @Override
	public Iterator iterator() {
		return this;
	}
        
        @Override
	public void remove() {
                throw new UnsupportedOperationException("Not supported yet.");
	}
        
        @Override
	public Vertex next() {
                Vertex result = _nextVertex;
                moveForwards();
                
		return result;
	}
        
        @Override
	public boolean hasNext() {
                return _nextVertex != null;
	}
        
        private void moveForwards() {
                try {
                        if (_resultSet.next()) {
                                _nextVertex = new MysqlVertex(_resultSet.getInt("vertexId"), this._graph);
                        } else {
                                _nextVertex = null; 
                                _resultSet.getStatement().close();
                                _resultSet.close();
                        }
                } catch (SQLException ex) {
                        Logger.getLogger(MysqlVertexlIterator.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
}

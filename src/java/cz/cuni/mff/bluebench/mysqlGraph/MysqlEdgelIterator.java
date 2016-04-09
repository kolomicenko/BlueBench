package cz.cuni.mff.bluebench.mysqlGraph;

import com.tinkerpop.blueprints.Edge;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helper iterator for MysqlGraph edges.
 * 
 * @author Sidney Shaw
 */
public class MysqlEdgelIterator implements Iterable, Iterator {
        private ResultSet _resultSet = null;
        private Edge _nextEdge = null;
        
        private MysqlGraph _graph;
        
        public MysqlEdgelIterator(ResultSet rs, MysqlGraph g) {
                this._resultSet = rs;
                
                this._graph = g;
                
                this.moveForwards();
        }
        
        @Override
	public Iterator iterator() {
		return this;
	}
        
        @Override
	public Edge next() {
                Edge result = _nextEdge;
                moveForwards();
                
		return result;
	}
        
        @Override
	public boolean hasNext() {
                return _nextEdge != null;
	}
        
        private void moveForwards() {
                try {
                        if (_resultSet.next()) {
                                _nextEdge = new MysqlEdge(_resultSet.getInt("edgeId"), this._graph,
                                        new MysqlVertex(_resultSet.getInt("sourceId"), this._graph),
                                        new MysqlVertex(_resultSet.getInt("targetId"), this._graph),
                                        _resultSet.getString("label"));
                        } else {
                                _nextEdge = null; 
                                _resultSet.getStatement().close();
                                _resultSet.close();
                        }
                } catch (SQLException ex) {
                        Logger.getLogger(MysqlEdgelIterator.class.getName()).log(Level.SEVERE, null, ex);
                }
        }

        @Override
        public void remove() {
                throw new UnsupportedOperationException("Not supported yet.");
        }
}

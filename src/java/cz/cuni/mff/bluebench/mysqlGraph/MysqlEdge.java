package cz.cuni.mff.bluebench.mysqlGraph;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MysqlGraph edge implementation.
 * 
 * @author Sidney Shaw
 */
public class MysqlEdge implements Edge{
        private long _id;
        private MysqlGraph _graph;
        private MysqlVertex _sourceVertex;
        private MysqlVertex _targetVertex;
        private String _label;
        
        public MysqlEdge(long id, MysqlGraph g) {
                this(id, g, null, null, null);
        }
        
        public MysqlEdge(long id, MysqlGraph g, MysqlVertex sourceVertex, MysqlVertex targetVertex, String label) {
                this._id = id;
                this._graph = g;
                this._sourceVertex = sourceVertex;
                this._targetVertex = targetVertex;
                this._label = label;
        }
        
        @Override
        public Vertex getVertex(Direction drctn) throws IllegalArgumentException {
                if (this._sourceVertex == null) {
                        this._load();
                }
                
                switch (drctn) {
                        case IN:
                                return this._targetVertex;
                        case OUT:
                                return this._sourceVertex;
                        default:
                                throw new IllegalArgumentException();
                }
        }
        
        @Override
        public String getLabel() {
                if (this._label == null) {
                        this._load();
                }
                
                return this._label;
        }
        
        @Override
        public Object getProperty(String propertyName) {
                String result = null;
                
                try {
                        Statement stmt = this._graph.getStmt();
                        ResultSet rs = stmt.executeQuery("select value from edgeProperties "
                                + "where edgeId = " + this._id + " and name = '" + propertyName + "'");
                        
                        if (rs.next()) {
                                result = rs.getString("value");
                        }
                } catch (SQLException ex) {
                        Logger.getLogger(MysqlGraph.class.getName()).log(Level.SEVERE, null, ex);
                }
                        
                return result;
        }

        @Override
        public Set<String> getPropertyKeys() {
                Set<String> result = new HashSet<>();
                
                try {
                        Statement stmt = this._graph.getStmt();
                        ResultSet rs = stmt.executeQuery("select name from edgeProperties "
                                + "where edgeId = " + this._id);
                        
                        while (rs.next()) {
                                result.add(rs.getString("name"));
                        }
                } catch (SQLException ex) {
                        Logger.getLogger(MysqlGraph.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                return result;
        }

        @Override
        public void setProperty(String propertyName, Object propertyValue) {
                try {
                        Statement stmt = this._graph.getStmt();
                        stmt.executeUpdate("replace edgeProperties(edgeId, name, value) "
                                + "values (" + this._id + ", '" + propertyName + "', '" + propertyValue + "')");
                } catch (SQLException ex) {
                        Logger.getLogger(MysqlGraph.class.getName()).log(Level.SEVERE, null, ex);
                }
        }

        @Override
        public Object removeProperty(String propertyName) {
                Object result = this.getProperty(propertyName);
                
                try {
                        Statement stmt = this._graph.getStmt();
                        stmt.executeUpdate("delete from edgeProperties "
                                + "where edgeId = " + this._id + " and name = '" + propertyName + "'");
                } catch (SQLException ex) {
                        Logger.getLogger(MysqlGraph.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                return result;
        }

        @Override
        public Object getId() {
                return this._id;
        }
        
        private void _load() {
                try {
                        Statement stmt = this._graph.getStmt();
                        ResultSet rs = stmt.executeQuery("select * from edges where edgeId = " + _id);
                        
                        this._label = rs.getString("label");
                        this._sourceVertex = new MysqlVertex(rs.getInt("sourceId"), this._graph);
                        this._targetVertex = new MysqlVertex(rs.getInt("targetId"), this._graph);
                } catch (SQLException ex) {
                        Logger.getLogger(MysqlGraph.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
        
        @Override
        public boolean equals(Object v){
                if (v instanceof MysqlEdge) {
                        return this._id == (long) ((MysqlVertex) v).getId();
                }
                return false;
        }

        @Override
        public int hashCode() {
                int hash = 3;
                hash = 29 * hash + (int) (this._id ^ (this._id >>> 32));
                return hash;
        }

}


package cz.cuni.mff.bluebench.mysqlGraph;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Query;
import com.tinkerpop.blueprints.Vertex;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MysqlGraph vertex implementation.
 * 
 * @author Sidney Shaw
 */
public class MysqlVertex implements Vertex {
        private long _id;
        private MysqlGraph _graph;
        
        public MysqlVertex(long id, MysqlGraph g) {
                this._id = id;
                this._graph = g;
        }
        
        @Override
        public Object getProperty(String propertyName) {
                String result = null;
                
                try {
                        Statement stmt = this._graph.getStmt();
                        ResultSet rs = stmt.executeQuery("select value from vertexProperties "
                                + "where vertexId = " + this._id + " and name = '" + propertyName + "'");
                        
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
                        ResultSet rs = stmt.executeQuery("select name from vertexProperties "
                                + "where vertexId = " + this._id);
                        
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
                        stmt.executeUpdate("replace vertexProperties(vertexId, name, value) "
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
                        stmt.executeUpdate("delete from vertexProperties "
                                + "where vertexId = " + this._id + " and name = '" + propertyName + "'");
                } catch (SQLException ex) {
                        Logger.getLogger(MysqlGraph.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                return result;
        }

        @Override
        public Object getId() {
                return this._id;
        }
        
        @Override
        public Iterable<Edge> getEdges(Direction drctn, String[] labels) {
                ResultSet rs = null;
                
                try {
                        Statement stmt = this._graph.getCon().createStatement();
                        String sel = null;
                        
                        switch (drctn) {
                                case IN:
                                        sel = "select * from edges where (targetId = " + this.getId() + ")";
                                        break;
                                case OUT:
                                        sel = "select * from edges where (sourceId = " + this.getId() + ")";
                                        break;
                                case BOTH:
                                        sel = "select * from edges where (targetId = " + this.getId() + " or "
                                                + "sourceId = " + this.getId() + ")";
                                        break;
                        }
                        
                        if (labels.length > 0) {
                                sel += " and label in (";
                                String delimeter = "";
                                
                                for (String label : labels) {
                                        sel += delimeter + "'" + label + "'";
                                        delimeter = ",";
                                }
                                
                                sel += ")";
                        }
                        
                        rs = stmt.executeQuery(sel);
                } catch (SQLException ex) {
                        Logger.getLogger(MysqlGraph.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                return new MysqlEdgelIterator(rs, this._graph);
        }

        @Override
        public Iterable<Vertex> getVertices(Direction drctn, String[] labels) {
                ResultSet rs = null;
                
                try {
                        Statement stmt = this._graph.getCon().createStatement();
                        String selIn = "select sourceId as vertexId from edges where targetId = " + this.getId();
                        String selOut = "select targetId as vertexId from edges where sourceId = " + this.getId();
                        
                        String selLabel = "";
                        
                        if (labels.length > 0) {
                                selLabel += " and label in (";
                                String delimeter = "";
                                
                                for (String label : labels) {
                                        selLabel += delimeter + label ;
                                        delimeter = ",";
                                }
                                
                                selLabel += ")";
                        }
                        
                        switch (drctn) {
                                case IN:
                                        rs = stmt.executeQuery(selIn + selLabel);
                                        break;
                                case OUT:
                                        rs = stmt.executeQuery(selOut + selLabel);
                                        break;
                                case BOTH:
                                        String selBoth = selIn + selLabel + " UNION " + selOut + selLabel;
                                        rs = stmt.executeQuery(selBoth);
                                        break;
                        }
                } catch (SQLException ex) {
                        Logger.getLogger(MysqlGraph.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                return new MysqlVertexlIterator(rs, this._graph);
        }

        @Override
        public Query query() {
                throw new UnsupportedOperationException("Not supported yet.");
        }
        
        @Override
        public boolean equals(Object v){
                if (v instanceof MysqlVertex) {
                        return this._id == (long) ((MysqlVertex) v).getId();
                }
                return false;
        }

        @Override
        public int hashCode() {
                return (int) this._id;
        }

}

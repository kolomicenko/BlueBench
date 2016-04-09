package cz.cuni.mff.bluebench.mysqlGraph;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Features;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.TransactionalGraph.Conclusion;
import com.tinkerpop.blueprints.Vertex;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MysqlGraph general implementation.
 * 
 * @author Sidney Shaw
 */
public class MysqlGraph implements Graph, TransactionalGraph, KeyIndexableGraph {
        
        private static final String DB_NAME = "bluebench_graph";
        
        private static final Features FEATURES = new Features();
        
        private Connection _con = null;
        
        private Statement _stmt = null;

        static {
                FEATURES.ignoresSuppliedIds = true;
        }
        
        public MysqlGraph(String serverAddress, String user, String password) {
                try {
                        this._con = DriverManager.getConnection("jdbc:mysql://" + serverAddress, user, password); 
                        this._stmt = _con.createStatement();
                        _stmt.executeUpdate("create database if not exists `" + DB_NAME + "`;");
                        _stmt.executeUpdate("use `" + DB_NAME + "`;");
                        _stmt.executeUpdate("create table if not exists vertices ("
                                + "vertexId INT NOT NULL AUTO_INCREMENT PRIMARY KEY);");
                        _stmt.executeUpdate("create table if not exists edges ("
                                + "edgeId INT NOT NULL AUTO_INCREMENT PRIMARY KEY, "
                                + "sourceId INT NOT NULL, "
                                + "targetId INT NOT NULL, "
                                + "label VARCHAR(256) NULL, "
                                + "index (sourceId), index (targetId), index (label));");
                        _stmt.executeUpdate("create table if not exists vertexProperties ("
                                + "vertexId INT NOT NULL, "
                                + "name VARCHAR(256) NOT NULL, "
                                + "value VARCHAR(256) NOT NULL, "
                                + "unique index unique_vertexId_name (vertexId, name), index (name));");
                        _stmt.executeUpdate("create table if not exists edgeProperties ("
                                + "edgeId INT NOT NULL, "
                                + "name VARCHAR(256) NOT NULL, "
                                + "value VARCHAR(256) NOT NULL, "
                                + "unique index unique_edgeId_name (edgeId, name), index (name));");
                        
                        _con.setAutoCommit(false);
                } catch (SQLException ex) {
                        Logger.getLogger(MysqlGraph.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
        
        public void delete() {
                try {
                        _stmt.executeUpdate("drop database `" + DB_NAME + "`;");
                } catch (SQLException ex) {
                        Logger.getLogger(MysqlGraph.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
        
        @Override
        public Features getFeatures(){
                return FEATURES;
        }

        @Override
        public Vertex addVertex(Object o) {
                long id = 0;
                
                try {
                        _stmt.executeUpdate("insert into vertices() values();", Statement.RETURN_GENERATED_KEYS);
                        
                        ResultSet rs = _stmt.getGeneratedKeys();
                        if (rs.next()){
                                id = rs.getLong(1);
                        }
                        
                        rs.close();
                } catch (SQLException ex) {
                        Logger.getLogger(MysqlGraph.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                return new MysqlVertex(id, this);
        }

        @Override
        public Vertex getVertex(Object o) {
                return new MysqlVertex((long) o, this);
        }

        @Override
        public void removeVertex(Vertex vertex) {
                try {
                        _stmt.executeUpdate("delete from vertices, vertexProperties "
                                + "using vertices natural left join vertexProperties "
                                + "where vertexId = " + vertex.getId());                        
                        _stmt.executeUpdate("delete from edges, edgeProperties "
                                + "using edges natural left join edgeProperties  "
                                + "where sourceId = " + vertex.getId() + " or "
                                + "targetId = " + vertex.getId());
                } catch (SQLException ex) {
                        Logger.getLogger(MysqlGraph.class.getName()).log(Level.SEVERE, null, ex);
                }
        }

        @Override
        public Iterable<Vertex> getVertices() {
                ResultSet rs = null;
                
                try {
                        Statement stmt = _con.createStatement();
                        rs = stmt.executeQuery("select * from vertices;");
                } catch (SQLException ex) {
                        Logger.getLogger(MysqlGraph.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                return new MysqlVertexlIterator(rs, this);
        }

        @Override
        public Iterable<Vertex> getVertices(String propertyName, Object propertyValue) {
                ResultSet rs = null;
                
                try {
                        Statement stmt = _con.createStatement();
                        rs = stmt.executeQuery("select vertexId from vertexProperties "
                                + "where name = '" + propertyName + "' and value = '" + propertyValue + "'");
                } catch (SQLException ex) {
                        Logger.getLogger(MysqlGraph.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                return new MysqlVertexlIterator(rs, this);
        }

        @Override
        public Edge addEdge(Object o, Vertex vertex, Vertex vertex1, String label) {
                long id = 0;
                
                try {
                        _stmt.executeUpdate("insert into edges(sourceId, targetId, label) "
                                + "values(" + vertex.getId() + ", " + vertex1.getId() + ", '" + label + "');", 
                                Statement.RETURN_GENERATED_KEYS);
                        
                        ResultSet rs = _stmt.getGeneratedKeys();
                        if (rs.next()){
                                id = rs.getLong(1);
                        }
                        
                        rs.close();
                } catch (SQLException ex) {
                        Logger.getLogger(MysqlGraph.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                return new MysqlEdge(id, this, (MysqlVertex) vertex, (MysqlVertex) vertex1, label);
        }

        @Override
        public Edge getEdge(Object o) {
                return new MysqlEdge((long) o, this);
        }

        @Override
        public void removeEdge(Edge edge) {
                try {
                        _stmt.executeUpdate("delete from edges, edgeProperties "
                                + "using edges natural left join edgeProperties "
                                + "where edgeId = " + edge.getId());
                } catch (SQLException ex) {
                        Logger.getLogger(MysqlGraph.class.getName()).log(Level.SEVERE, null, ex);
                }
        }

        @Override
        public void shutdown() {
                try {
                        this._con.commit();
                } catch (SQLException ex) {
                        Logger.getLogger(MysqlGraph.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
        
        @Override
        public Iterable<Edge> getEdges() {
                ResultSet rs = null;
                
                try {
                        Statement stmt = _con.createStatement();
                        rs = stmt.executeQuery("select * from edges;");
                } catch (SQLException ex) {
                        Logger.getLogger(MysqlGraph.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                return new MysqlEdgelIterator(rs, this);
        }
        
        @Override
        public Iterable<Edge> getEdges(String propertyName, Object propertyValue) {
                ResultSet rs = null;
                
                try {
                        Statement stmt = _con.createStatement();
                        rs = stmt.executeQuery("select * from edges natural join edgeProperties "
                                + "where name = '" + propertyName + "' and value = '" + propertyValue + "'");
                } catch (SQLException ex) {
                        Logger.getLogger(MysqlGraph.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                return new MysqlEdgelIterator(rs, this);
        }

        @Override
        public void stopTransaction(Conclusion cnclsn) {
                try {
                        switch (cnclsn) {
                                case FAILURE:
                                        this._con.rollback();
                                        break;
                                case SUCCESS:
                                        this._con.commit();
                                        break;
                        }
                        
                } catch (SQLException ex) {
                        Logger.getLogger(MysqlGraph.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
        
        public Connection getCon() {
                return this._con;
        }
        
        public Statement getStmt() {
                return this._stmt;
        }

        @Override
        public <T extends Element> void dropKeyIndex(String string, Class<T> type) {
                // indexing is automatic
        }

        @Override
        public <T extends Element> void createKeyIndex(String string, Class<T> type) {
                // indexing is automatic
        }

        @Override
        public <T extends Element> Set<String> getIndexedKeys(Class<T> type) {
                throw new UnsupportedOperationException("Not supported.");
        }
        
}

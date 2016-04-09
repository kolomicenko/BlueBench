package cz.cuni.mff.bluebench;

import be.datablend.blueprints.impls.mongodb.MongoDBGraph;
import com.infinitegraph.AccessMode;
import com.infinitegraph.ConfigurationException;
import com.infinitegraph.GraphDatabase;
import com.infinitegraph.GraphFactory;
import com.infinitegraph.StorageException;
import com.infinitegraph.Transaction;
import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.bench.Bench;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.impls.arangodb.ArangoDBGraph;
import com.tinkerpop.blueprints.impls.arangodb.client.ArangoDBException;
import com.tinkerpop.blueprints.impls.dex.DexGraph;
import com.tinkerpop.blueprints.impls.ig.IGGraph;
import com.tinkerpop.blueprints.impls.neo4j.Neo4jGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.sail.impls.NativeStoreSailGraph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import cz.cuni.mff.bluebench.dummyGraph.DummyGraph;
import cz.cuni.mff.bluebench.mysqlGraph.MysqlGraph;
import java.io.File;

import com.infinitegraph.indexing.*;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * This enum contains basic configuration for various database engines.
 * 
 * @author Sidney Shaw
 */
public enum DB {
        Dummy ("DummyGraph", DummyGraph.class, "", null, false),
        Tinker ("TinkerGraph", TinkerGraph.class, "", null, false),
        Neo4j ("Neo4j", Neo4jGraph.class, "", null, true),
        Orient ("OrientDB", OrientGraph.class, "local:", null, true),
        DEX ("DEX", DexGraph.class, "", Bench.benchProperties.getProperty(Bench.DEX_CFG_PATH), true),
        NativeSail("NativeSail", NativeStoreSailGraph.class, "", null, true),
        Arango("ArangoDB", ArangoDBGraph.class, "", null, true),
        Mongo("MongoDB", MongoDBGraph.class, "", null, true),
        Titan("Titan", TitanGraph.class, "", null, true),
        Mysql("MysqlGraph", MysqlGraph.class, "", null, true),
        Infinite("InfiniteGraph", IGGraph.class, "/opt/InfiniteGraph/3.0/data/", // same value as in IG_CFG_PATH file
                Bench.benchProperties.getProperty(Bench.IG_CFG_PATH), true);
        
        public final String name;
        private final Class<?> graphClass;
        private final String graphPathPrefix;
        public final String cfgPath;
        public final boolean isPersistent;
        
        /**
         * Returns the classname of the Graph.
         * 
         * @return 
         */
        public String getGraphClass() {
                return graphClass.getName();
        }
        
        /**
         * Gets the path where the database can be obtained.
         * 
         * @return 
         */
        public String getGraphPath() {
                return graphPathPrefix + this.getGraphDir();
        }
        
        /**
         * Gets the graph directory.
         * 
         * @return 
         */
        public String getGraphDir() {
                if (this == Infinite) {
                        return this.graphPathPrefix;
                }
                
                String dir = Bench.benchProperties.getProperty(Bench.DB_DATA_DIRECTORY) + name;
                
                if (this != DEX) {
                        dir += "/";
                }
                
                return dir;
        }
        
        /**
         * Sets the directory for the persistent file storage.
         * 
         * @param resultsDir
         * @return 
         */
        public String getGraphDirectory(String resultsDir) {
                return resultsDir + name + "/";
        }
        
        /**
         * Creates the graph.
         * 
         * @param path
         * @return
         * @throws Exception 
         */
        public Graph createGraph(String path) throws Exception {
                switch (this) {
                        case Arango:
                                return new ArangoDBGraph("localhost", 8529, "arango-graph", 
                                                "arango-vertices", "arango-edges");
                        case Mongo:
                                return new MongoDBGraph("localhost", 27017);
                        case Titan:
                                return TitanFactory.open(path);
                        case Mysql:
                                return new MysqlGraph(
                                        Bench.benchProperties.getProperty(Bench.MYSQL_SERVER),
                                        Bench.benchProperties.getProperty(Bench.MYSQL_USER),
                                        Bench.benchProperties.getProperty(Bench.MYSQL_PASSWD)
                                );
                        case Infinite:
                                String dbFile = this.getGraphDir()
                                        + Bench.benchProperties.getProperty(Bench.IG_DB_NAME) + ".boot";
                                
                                if (!new File(dbFile).exists()) {
                                        File graphDir = new File(this.getGraphDir());
                                        if (!graphDir.isDirectory()) {
                                                graphDir.mkdir();
                                        }
                                        
                                        
                                        GraphFactory.create(
                                                Bench.benchProperties.getProperty(Bench.IG_DB_NAME),
                                                this.cfgPath
                                        );
                                }
                                
                                return new IGGraph(dbFile);
                }
                
                return null;
        }
        
        /**
         * Physically deletes the graph.
         * 
         * @param graph
         * @return 
         */
        public boolean deleteGraph(Graph graph) {
                switch (this) {
                        case Arango:
                                try {
                                        return ((ArangoDBGraph) graph).delete();
                                } catch (ArangoDBException e) {
                                }
                        case Mongo:
                                ((MongoDBGraph) graph).clear();
                                return true;
                        case Mysql:
                                ((MysqlGraph) graph).delete();
                                return true;
                        case Infinite:
                                graph.shutdown();
                                try {
//                                        GraphFactory.delete(
//                                                Bench.benchProperties.getProperty(Bench.IG_DB_NAME),
//                                                this.cfgPath
//                                        );
                                        GraphDatabase nativeDb = GraphFactory.open(
                                                Bench.benchProperties.getProperty(Bench.IG_DB_NAME), 
                                                this.cfgPath
                                        );
                                        
                                        Transaction tx = nativeDb.beginTransaction(AccessMode.READ_WRITE);
                                        try {
                                                // drop all graph indexes
                                                IndexDescription[] indexes = IndexManager.listAllGraphIndexes();
                                                for (IndexDescription index : indexes) {
                                                        IndexManager.dropGraphIndex(index.getIndexName());
                                                }
                                        } catch (IndexException ex) {
                                                Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                        tx.commit();
                                        tx.complete();
                                        
                                        tx = nativeDb.beginTransaction(AccessMode.READ_WRITE);
                                        nativeDb.clear();
                                        tx.commit();
                                        tx.complete();
                                        
                                        nativeDb.close();
                                }
                                catch (ConfigurationException | StorageException e){
                                }
                }
                
                return false;
        }
        
        /**
         * Constructor.
         * 
         * @param name
         * @param graphClass
         * @param graphPathPrefix
         * @param cfgPath
         * @param isPersistent 
         */
        DB(String name, Class<?> graphClass, String graphPathPrefix, String cfgPath, boolean isPersistent) {
                this.name = name;
                this.graphClass = graphClass;
                this.graphPathPrefix = graphPathPrefix;
                this.cfgPath = cfgPath;
                this.isPersistent = isPersistent;
        }
        
}


package cz.cuni.mff.bluebench.operations;

import com.infinitegraph.AccessMode;
import com.infinitegraph.ConfigurationException;
import com.infinitegraph.GraphDatabase;
import com.infinitegraph.GraphFactory;
import com.infinitegraph.StorageException;
import com.infinitegraph.Transaction;
import com.infinitegraph.navigation.*;
import com.infinitegraph.navigation.policies.*;
import com.infinitegraph.policies.PolicyChain;
import com.orientechnologies.orient.core.command.traverse.OTraverse;
import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.filter.OSQLPredicate;
import com.sparsity.dex.algorithms.Context;
import com.sparsity.dex.gdb.Database;
import com.sparsity.dex.gdb.DexConfig;
import com.sparsity.dex.gdb.DexProperties;
import com.sparsity.dex.gdb.EdgesDirection;
import com.sparsity.dex.gdb.Objects;
import com.sparsity.dex.gdb.Session;
import com.tinkerpop.bench.Bench;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.neo4j.Neo4jVertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import cz.cuni.mff.bluebench.DB;
import cz.cuni.mff.bluebench.operation.NativeOperation;
import cz.cuni.mff.bluebench.utils.IGVertex;
import cz.cuni.mff.bluebench.utils.GraphUtils;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.kernel.Traversal;

/**
 * Performs a BFS traversal from a provided vertex (native versions of the algorithms).
 * 
 * @author Sidney Shaw
 */
public class TraversalNative extends NativeOperation {
        
        private Vertex startingVertex = null;
        private String[] labels = new String[]{};
        private Direction direction = Direction.OUT;
        private int maxHops = 5;
        
        @Override
	public void initNative(String[] args) {
                this.startingVertex = GraphUtils.getVertex(Integer.valueOf(args[0]), getGraph());
	}
        
        @Override
        public Object execNative() {
                HashSet<Vertex> result = GraphUtils.BFS(startingVertex, maxHops, direction, labels);
                
                return result.size();
        }
        
        @Override
        protected NativeOperation getNativeDexImpl() {
                return new Dex(this);
        }
        
        private class Dex extends NativeOperation {
                private TraversalNative parent;
                private long startingVertexId;
                private Session sess;
                private com.sparsity.dex.gdb.Dex dex;
                private Database db;
                
                public Dex(TraversalNative parent) {
                        this.parent = parent;
                }
                
                @Override
                public void initNative(String[] args){
                        startingVertexId = (long) GraphUtils.getVertex(Integer.valueOf(args[0]), parent.getGraph()).getId();
                        
                        parent.getGraphDescriptor().shutdownGraph();
                        
                        DexConfig cfg = new DexConfig();
                        DexProperties.load(DB.DEX.cfgPath);

                        dex = new com.sparsity.dex.gdb.Dex(cfg);
                        db  = null;
                        try {
                                db = dex.open(DB.DEX.getGraphDir(), true);
                        } catch (FileNotFoundException ex) {
                                Logger.getLogger(TraversalNative.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        
                        sess = db.newSession();
                }
                
                @Override
                public Object execNative() {
                        int size;
                        try (Objects obj = Context.compute(sess, startingVertexId, null, null, EdgesDirection.Outgoing, maxHops, true)) {
                                size = obj.size();
                        }
                        
                        return size - 1; //for the starting vertex
                }
                
                @Override
                public void concludeNative() {
                        sess.close();
                        db.close();
                        dex.close();
                        
                        try {
                                parent.getGraphDescriptor().openGraph();
                        } catch (Exception ex) {
                                Logger.getLogger(TraversalNative.class.getName()).log(Level.SEVERE, null, ex);
                        }
                }
        }
        
        @Override
        protected NativeOperation getNativeNeo4jImpl() {
                return new Neo4j(this);
        }
        
        private class Neo4j extends NativeOperation {
                private TraversalNative parent;
                private TraversalDescription td;
                private Node startingVertex;
                
                public Neo4j(TraversalNative parent) {
                        this.parent = parent;
                }
                
                public class Friend implements RelationshipType {

                        @Override
                        public String name() {
                                return "label:_friend";
                        }
                }
                
                public class Family implements RelationshipType {

                        @Override
                        public String name() {
                                return "label:_family";
                        }
                }
                
                @Override
                public void initNative(String[] args) {
                        startingVertex = ((Neo4jVertex) GraphUtils.getVertex(Integer.valueOf(args[0]), parent.getGraph())).getRawVertex();
                        
                        td = Traversal.description()
                                .relationships( new Friend(), org.neo4j.graphdb.Direction.OUTGOING )
                                .relationships( new Family(), org.neo4j.graphdb.Direction.OUTGOING )
                                .breadthFirst()
                                .evaluator(Evaluators.toDepth(maxHops))
                        ;
                }
                
                @Override
                public Object execNative() {
                        Traverser tr = td.traverse(this.startingVertex);
                        
                        int cnt = 0;
                        for (Path path : tr) {
                                cnt++;
                        }
                        
                        return cnt - 1; // for the starting vertex
                }
        }
        
        @Override
        protected NativeOperation getNativeOrientImpl() {
                return new Orient(this);
        }
        
        private class Orient extends NativeOperation {
                // http://code.google.com/p/orient/wiki/JavaTraverse
                
                private ODocument startingVertex;
                private OGraphDatabase db;
                private TraversalNative parent;
                private OTraverse traverse;
                
                public Orient(TraversalNative parent) {
                        this.parent = parent;
                }
                
                @Override
                public void initNative(String[] args) {
                        startingVertex = ((OrientVertex) GraphUtils.getVertex(Integer.valueOf(args[0]), parent.getGraph())).getRawVertex();
                        
                        String[] a = startingVertex.fieldNames();
                        
                        db = ((OrientGraph) parent.getGraph()).getRawGraph();
                        
                        traverse = new OTraverse()
                                //.field("in")
                                .field("out")
                                .target(this.startingVertex)
//                                .predicate(
//                                        new OCommandPredicate() {
//                                                @Override
//                                                public Object evaluate(ORecord<?> iRecord, ODocument od, OCommandContext iContext) {
//                                                        return ((Integer) iContext.getVariable("depth")) <= maxHops * 2 + 1;
//                                                }
//                                        })
                                .predicate(new OSQLPredicate("$depth <= " + maxHops * 2 + 1))
                        ;
                }
                
                @Override
                public Object execNative() {
                        List<OIdentifiable> list = traverse.execute();
                        
                        int cnt = 0;
                        for (OIdentifiable id : list) {
                                if (this.db.isVertex((ODocument) id.getRecord())) {
                                        cnt++;
                                }
                        }
                        
                        return cnt;
                }
        }
        
        @Override
        protected NativeOperation getNativeIGImpl() {
                return new IG();
        }
        
        private class IG extends TraversalNative {
                
                private class PathResultsHandler implements NavigationResultHandler 
                {
                        @Override
                        public void handleResultPath(com.infinitegraph.navigation.Path result, Navigator navigator) {
                                visitedVertices.add(result.getFinalHop().getVertex().getId());
                        }
                        
                        @Override
                        public void handleNavigatorFinished(Navigator navigator) {}
                }
                
                private TraversalNative parent;
                
                private GraphDatabase nativeDb = null;
                private Transaction tx = null;
                private Navigator myNavigator;
                private Set<Long> visitedVertices = new HashSet<>();
                
                @Override
                public void initNative(String[] args) {
                        // http://wiki.infinitegraph.com/3.0/w/index.php?title=Tutorial:_Using_the_Navigator
                        
                        Vertex startingVertex = GraphUtils.getVertex(Integer.valueOf(args[0]), getGraph());
                        
                        long startingVertexId = (long) GraphUtils.getRawId(args[0]);
                        
                        startingVertexId = (long) startingVertex.getId();
                        
                        this.getGraphDescriptor().shutdownGraph();
                        try {
                                nativeDb = GraphFactory.open(
                                        Bench.benchProperties.getProperty(Bench.IG_DB_NAME), 
                                        DB.Infinite.cfgPath
                                );
                                
                        } catch (ConfigurationException | StorageException ex) {
                                Logger.getLogger(TraversalNative.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        
                        tx = nativeDb.beginTransaction(AccessMode.READ);
                        
                        IGVertex startingVertexBB = (IGVertex) nativeDb.getVertex(startingVertexId);
                        
                        PathResultsHandler resultHandler = new PathResultsHandler();
                        
                        PolicyChain myPolicies = new PolicyChain(new MaximumPathDepthPolicy(maxHops));
                        
                        
                        myNavigator = startingVertexBB.navigate(null, Guide.SIMPLE_BREADTH_FIRST, Qualifier.FOREVER, Qualifier.ANY, myPolicies, resultHandler);
                        
                }
                
                @Override
                public Object execNative() {
                        
                        myNavigator.start();
                        myNavigator.stop();

                        return visitedVertices.size();
                }
                
                @Override
                public void concludeNative() {
                        tx.commit();
                        tx.complete();
                        nativeDb.close();
                        
                        try {
                                this.getGraphDescriptor().openGraph();
                        } catch (Exception ex) {
                                Logger.getLogger(TraversalNative.class.getName()).log(Level.SEVERE, null, ex);
                        }
                }
        }
}


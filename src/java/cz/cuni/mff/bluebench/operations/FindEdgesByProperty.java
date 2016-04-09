package cz.cuni.mff.bluebench.operations;

import com.infinitegraph.AccessMode;
import com.infinitegraph.ConfigurationException;
import com.infinitegraph.GraphDatabase;
import com.infinitegraph.GraphException;
import com.infinitegraph.GraphFactory;
import com.infinitegraph.Query;
import com.infinitegraph.StorageException;
import com.infinitegraph.Transaction;
import com.tinkerpop.bench.Bench;
import com.tinkerpop.blueprints.Edge;
import cz.cuni.mff.bluebench.DB;
import cz.cuni.mff.bluebench.operation.NativeOperation;
import cz.cuni.mff.bluebench.utils.IGEdgeWithProperties;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Operation which searches the graph for edges with a specified property.
 * 
 * @author Sidney Shaw
 */
public class FindEdgesByProperty extends NativeOperation{
        
        protected String propertyValue = null;
        protected String propertyKey = null;
        
        @Override
	public void initNative(String[] args) {
                this.propertyKey = Bench.benchProperties.getProperty(Bench.GRAPH_PROPERTY_COLOR);
                this.propertyValue = args[0];
	}

        @Override
	public Object execNative(){
                int count = 0;
                for (Edge e : this.getGraph().getEdges(this.propertyKey, this.propertyValue)) {
                        count++;
                }
                        
                return count;
	}
        
        @Override
        protected NativeOperation getNativeIGImpl() {
                return new FindEdgesByProperty.IG();
        }

        private class IG extends FindEdgesByProperty {
                
                private GraphDatabase nativeDb = null;
                private Transaction tx = null;
                
                @Override
                public void initNative(String[] args) {
                        super.initNative(args);
                        
                        this.getGraph().shutdown();
                        
                        try {
                                nativeDb = GraphFactory.open(
                                        Bench.benchProperties.getProperty(Bench.IG_DB_NAME), 
                                        DB.Infinite.cfgPath
                                );
                                
                        } catch (ConfigurationException | StorageException ex) {
                                Logger.getLogger(LoadGraphML.class.getName()).log(Level.SEVERE, null, ex);
                        }
                }
                
                @Override
                public Object execNative(){
                        tx = nativeDb.beginTransaction(AccessMode.READ);
                        
                        int count = 0;
                        
                        Query<IGEdgeWithProperties> edgeQuery = 
                                nativeDb.createQuery(IGEdgeWithProperties.class.getName(), 
                                this.propertyKey + " == '" + this.propertyValue + "'");
                        try {
                                Iterator edgeItr = (Iterator) edgeQuery.execute();
                                
                                while (edgeItr.hasNext()) {
                                        count++;
                                        edgeItr.next();
                                }
                        } catch (GraphException ex) {
                                Logger.getLogger(FindEdgesByProperty.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        
                        tx.complete();
                        
                        return count;
                }
                
                @Override
                public void concludeNative() {
                        nativeDb.close();
                        
                        try {
                                this.getGraphDescriptor().openGraph();
                        } catch (Exception ex) {
                                Logger.getLogger(LoadGraphML.class.getName()).log(Level.SEVERE, null, ex);
                        }
                }
        }
}

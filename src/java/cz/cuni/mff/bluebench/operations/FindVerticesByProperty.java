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
import com.tinkerpop.blueprints.Vertex;
import cz.cuni.mff.bluebench.DB;
import cz.cuni.mff.bluebench.operation.NativeOperation;
import cz.cuni.mff.bluebench.utils.IGEdgeWithProperties;
import cz.cuni.mff.bluebench.utils.IGVertexWithProperties;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Operation which searches the graph for vertices with a specified property.
 * 
 * @author Sidney Shaw
 */
public class FindVerticesByProperty extends NativeOperation{
        
        protected String propertyValue = null;
        protected String propertyKey = null;
        
        @Override
	public void initNative(String[] args) {
                this.propertyKey = Bench.benchProperties.getProperty(Bench.GRAPH_PROPERTY_NAME);
                this.propertyValue = args[0];
	}

        @Override
	public Object execNative(){
                int count = 0;
                for (Vertex v : this.getGraph().getVertices(this.propertyKey, this.propertyValue)) {
                        count++;
                }
                        
                return count;
	}
        
        @Override
        protected NativeOperation getNativeIGImpl() {
                return new FindVerticesByProperty.IG();
        }

        private class IG extends FindVerticesByProperty {
                
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
                                nativeDb.createQuery(IGVertexWithProperties.class.getName(), 
                                this.propertyKey + " == '" + this.propertyValue + "'");
                        try {
                                Iterator vertexItr = (Iterator) edgeQuery.execute();
                                
                                while (vertexItr.hasNext()) {
                                        count++;
                                        vertexItr.next();
                                }
                        } catch (GraphException ex) {
                                Logger.getLogger(FindVerticesByProperty.class.getName()).log(Level.SEVERE, null, ex);
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

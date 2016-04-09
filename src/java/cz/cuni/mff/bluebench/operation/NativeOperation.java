package cz.cuni.mff.bluebench.operation;

import com.tinkerpop.bench.operation.Operation;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.impls.dex.DexGraph;
import com.tinkerpop.blueprints.impls.ig.IGGraph;
import com.tinkerpop.blueprints.impls.neo4j.Neo4jGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.thinkaurelius.titan.core.TitanGraph;

/**
 * Native operation class.
 * 
 * @author Sidney Shaw
 */
public abstract class NativeOperation extends Operation{
        private NativeOperation callerObj = null;
        
        /**
         * Decides which native implementation should be used.
         */
        private void decideNativeImpl() {
                Graph actualGraph = this.getGraph();
                
                if (actualGraph instanceof Neo4jGraph) {
                        this.callerObj = this.getNativeNeo4jImpl();
                        return;
                }
                
                if (actualGraph instanceof OrientGraph) {
                        this.callerObj = this.getNativeOrientImpl();
                        return;
                }
                
                if (actualGraph instanceof DexGraph) {
                        this.callerObj = this.getNativeDexImpl();
                        return;
                }
                
                if (actualGraph instanceof IGGraph) {
                        this.callerObj = this.getNativeIGImpl();
                        return;
                }
                
                if (actualGraph instanceof TitanGraph) {
                        this.callerObj = this.getNativeTitanImpl();
                        return;
                }
                
                this.callerObj = this;
        }
        
        /**
         * Executes the operation.
         * 
         * @throws Exception 
         */
        @Override
	protected final void onExecute() throws Exception {
                try {
                        this.setResult(this.callerObj.execNative());
                        
		} catch (Exception e) {
			throw e;
		}
	}
        
        /**
         * Initializes the operation.
         * 
         * @param args 
         */
        @Override
	protected final void onInitialize(String[] args) {
                this.decideNativeImpl();
                this.callerObj.setGraphDescriptor(this.getGraphDescriptor());
                
                this.callerObj.initNative(args);
        }
        
        /**
         * Concludes the operation.
         */
        @Override
        protected final void onConclude() {
                this.callerObj.concludeNative();
        }
        
        protected NativeOperation getNativeOrientImpl() {
                return this;
        }
        
        protected NativeOperation getNativeDexImpl() {
                return this;
        }
        
        protected NativeOperation getNativeNeo4jImpl() {
                return this;
        }
        
        protected NativeOperation getNativeIGImpl() {
                return this;
        }
        
        protected NativeOperation getNativeTitanImpl() {
                return this;
        }
        
        /**
         * Executes the native implementation.
         * 
         * @return 
         */
        public abstract Object execNative();
        
        /**
         * Initializes the native operation.
         * 
         * @param args 
         */
        public abstract void initNative(String[] args);
        
        /**
         * Concludes the native operation.
         */
        public void concludeNative() {}
}

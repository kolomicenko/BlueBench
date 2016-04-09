package cz.cuni.mff.bluebench.factories;

import com.tinkerpop.bench.Bench;
import com.tinkerpop.bench.StatisticsHelper;
import com.tinkerpop.bench.evaluators.EvaluatorOutDegree;
import com.tinkerpop.bench.operationFactory.OperationDetails;
import com.tinkerpop.bench.operationFactory.OperationFactoryBase;
import com.tinkerpop.blueprints.Vertex;
import cz.cuni.mff.bluebench.utils.GraphUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The factory which can contain single operations.
 *
 * @author Sidney Shaw
 */
public class OperationFactorySimple extends OperationFactoryBase {
        
	private int opCount = -1;
	private String tag = null;
        private List<Integer> sampleIds = null;
        private Class<?> operationType = null;
        
        /**
         * Constructor.
         * 
         * @param operationType
         * @param opCount
         * @param tag 
         */
        public OperationFactorySimple(Class<?> operationType, int opCount, String tag) {
                this.opCount = opCount;
                this.operationType = operationType;
                this.tag = tag;
        }
        
        /**
         * Initialization method.
         */
        @Override
	public void onInitialize() {
                // Select vertices randomly (probability of selecting a vertex is proportional to its out-degree)
//		Vertex[] vertices = (Vertex[])StatisticsHelper.getSampleVertexIds(getGraph(), new EvaluatorOutDegree(), opCount);
//		vertexSamples = new ArrayList<>(Arrays.asList(vertices));
                sampleIds = GraphUtils.getSampleIds(opCount);
	}

        /**
         * Tells whether there are more operations to be processed.
         * @return 
         */
	@Override
	public boolean hasNext() {
		return sampleIds.isEmpty() == false;
	}

        /**
         * Returns the next operation's details.
         * 
         * @return
         * @throws Exception 
         */
	@Override
	protected OperationDetails onCreateOperation() throws Exception {
		return new OperationDetails(new String[] {sampleIds.remove(0).toString()}, operationType, tag);
	}
}

package cz.cuni.mff.bluebench.factories;

import com.tinkerpop.bench.operationFactory.OperationDetails;
import com.tinkerpop.bench.operationFactory.OperationFactoryBase;
import cz.cuni.mff.bluebench.operation.OperationArgsProvider;
import cz.cuni.mff.bluebench.utils.GraphUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * The factory which can contain multiple operations.
 *
 * @author Sidney Shaw
 */
public class OperationFactoryMultiple extends OperationFactoryBase {
	private int timesRepeat = -1;
	private String tag = null;
        private List<String[]> sampleArgs = null;
        private ArrayList<Class<?>> operationTypes = null;
        private Iterator<String[]> sampleIterator = null;
        private OperationArgsProvider argProvider = null;
        
        /**
         * Constructor.
         * 
         * @param operationTypes
         * @param timesRepeat
         * @param tag
         * @param argProvider 
         */
        public OperationFactoryMultiple(Class<?>[] operationTypes, int timesRepeat, String tag, OperationArgsProvider argProvider) {
                this.timesRepeat = timesRepeat;
                this.operationTypes = new ArrayList<>(Arrays.asList(operationTypes));
                this.tag = tag;
                this.argProvider = argProvider;
        }
        
        /**
         * Initialization method.
         */
        @Override
	public void onInitialize() {
                // Select vertices randomly (probability of selecting a vertex is proportional to its out-degree)
//		Vertex[] vertices = (Vertex[])StatisticsHelper.getSampleVertexIds(getGraph(), new EvaluatorOutDegree(), timesRepeat);
//		sampleIds = new ArrayList<>(Arrays.asList(vertices));
                sampleArgs = argProvider.getArgList(timesRepeat, operationTypes.get(0).getSimpleName() + tag);
                sampleIterator = sampleArgs.iterator();
                
	}

        /**
         * Tells whether there are more operations to be processed.
         * @return 
         */
	@Override
	public boolean hasNext() {
		return operationTypes.size() > 1 || sampleIterator.hasNext();
	}

        /**
         * Returns the next operation's details.
         * 
         * @return
         * @throws Exception 
         */
	@Override
	protected OperationDetails onCreateOperation() throws Exception {
                if (!sampleIterator.hasNext()) {
                        sampleIterator = sampleArgs.iterator();
                        operationTypes.remove(0);
                }
                
		return new OperationDetails(sampleIterator.next(), operationTypes.get(0), tag);
	}
}

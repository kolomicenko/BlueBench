package cz.cuni.mff.bluebench.operation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Makes sure the same operation gets the same arguments in any consequent runs (i.e. in another benchmark).
 * 
 * @author Sidney Shaw
 */
public abstract class OperationArgsProvider {
        static Map<String, List<String[]>> argsCache = new HashMap<>();
        
        /**
         * Returns the list of arguments for the operation.
         * 
         * @param sampleCount
         * @param operationId
         * @return 
         */
        public List<String[]> getArgList(int sampleCount, String operationId) {
                if (!argsCache.containsKey(operationId)) {
                     argsCache.put(operationId, createArgList(sampleCount));   
                }
                
                return argsCache.get(operationId);
        }
        
        /**
         * Creates the list of operation arguements.
         * 
         * @param sampleCount
         * @return 
         */
        protected abstract List<String[]> createArgList(int sampleCount);
}

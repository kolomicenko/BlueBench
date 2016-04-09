package cz.cuni.mff.bluebench.operation;

import cz.cuni.mff.bluebench.utils.GraphUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides an arbitrary number of random vertex ids.
 * 
 * @author Sidney Shaw
 */
public class VertexIdsArgProvider extends OperationArgsProvider {
        private int idCount;
        
        public VertexIdsArgProvider(int idCount) {
                this.idCount = idCount;
        }

        @Override
        public List<String[]> createArgList(int sampleCount) {
                List<String[]> result = new ArrayList<>();
                
                List<String> strIds = new ArrayList<>();
                for (Integer id : GraphUtils.getSampleIds(sampleCount * idCount)) {
                        strIds.add(id.toString());
                        
                        if (strIds.size() >= idCount) {
                                result.add(strIds.toArray(new String[idCount]));
                                strIds.clear();
                        }
                }
                
                return result;
        }
}

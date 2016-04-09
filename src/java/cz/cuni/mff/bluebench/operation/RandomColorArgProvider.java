package cz.cuni.mff.bluebench.operation;

import com.tinkerpop.bench.Bench;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Provides a random color argument.
 * 
 * @author Sidney Shaw
 */
public class RandomColorArgProvider extends OperationArgsProvider {

        @Override
        public List<String[]> createArgList(int sampleCount) {
                List<String[]> result = new ArrayList<>();
                Random rand = new Random();
                
                String[] propertyValues = Bench.benchProperties.getProperty(Bench.GRAPH_PROPERTY_COLORS).split(",");
                
                for (int i = 0; i < sampleCount; i++) {
                        result.add(new String[]{propertyValues[rand.nextInt(propertyValues.length)]});
                }
                
                return result;
        }
}

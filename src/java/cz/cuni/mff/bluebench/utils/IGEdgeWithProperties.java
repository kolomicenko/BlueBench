
package cz.cuni.mff.bluebench.utils;

/**
 * A helper class for an InfiniteGraph edge with properties.
 * 
 * @author Sidney Shaw
 */
public class IGEdgeWithProperties extends IGEdge {
        private long _weight;
        private String _color;
        
        public IGEdgeWithProperties(long weight, String color) {
                markModified();
                this._weight = weight;
                this._color = color;
        }
}

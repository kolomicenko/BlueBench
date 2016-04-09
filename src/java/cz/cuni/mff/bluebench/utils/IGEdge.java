package cz.cuni.mff.bluebench.utils;

import com.tinkerpop.blueprints.impls.ig.util.LabeledEdge;

/**
 * A helper class for an InfiniteGraph edge.
 * 
 * @author Sidney Shaw
 */
public class IGEdge extends LabeledEdge {
        public IGEdge() {
                markModified();
        }
}

package cz.cuni.mff.bluebench.utils;

/**
 *
 * A helper class for an InfiniteGraph vertex with properties.
 * 
 * @author Sidney Shaw
 */
public class IGVertexWithProperties extends IGVertex {
        private long _age;
        private String _name;
        
        public IGVertexWithProperties(long age, String name) {
                markModified();
                this._age = age;
                this._name = name;
        }
}

package cz.cuni.mff.bluebench.dummyGraph;

import java.util.Iterator;

/**
 *
 * @author Sidney Shaw
 */
public class DummyIterator<T> implements Iterable<T>, Iterator<T> {
        public DummyIterator() {
        }
        
        @Override
	public Iterator<T> iterator() {
		return this;
	}
        
        @Override
	public void remove() {
	}
        
        @Override
	public T next() {
		return null;
	}
        
        @Override
	public boolean hasNext() {
		return false;
	}
}

package relop;

/**
 * The selection operator specifies which tuples to retain under a condition; in
 * Minibase, this condition is simply a set of independent predicates logically
 * connected by OR operators.
 */
public class Selection extends Iterator {

	Iterator child;
	Predicate[] mPreds;
	Tuple nextTuple;

	/**
	 * Constructs a selection, given the underlying iterator and predicates.
	 */
	public Selection(Iterator iter, Predicate... preds) {
		child = iter;
		mPreds = preds;
		this.schema = iter.schema;
	}

	/**
	 * Gives a one-line explaination of the iterator, repeats the call on any
	 * child iterators, and increases the indent depth along the way.
	 */
	public void explain(int depth) {
		indent(depth);
		System.out.println("Selection");
		child.explain(depth + 1);
	}

	/**
	 * Restarts the iterator, i.e. as if it were just constructed.
	 */
	public void restart() {
		child.restart();
	}

	/**
	 * Returns true if the iterator is open; false otherwise.
	 */
	public boolean isOpen() {
		return child.isOpen();
	}

	/**
	 * Closes the iterator, releasing any resources (i.e. pinned pages).
	 */
	public void close() {
		child.close();
	}

	/**
	 * Returns true if there are more tuples, false otherwise.
	 */
	public boolean hasNext() {
		if(nextTuple != null) {
			return true;
		}
		try {
			nextTuple = getNext();
			return true;
		} catch (IllegalStateException e) {
			return false;
		}
	}

	/**
	 * Gets the next tuple in the iteration.
	 * 
	 * @throws IllegalStateException
	 *             if no more tuples
	 */
	public Tuple getNext() {
		if(nextTuple != null) {
			Tuple tuple = nextTuple;
			nextTuple = null;
			return tuple;
		}
		while(child.hasNext()) {
			Tuple tuple = child.getNext();
			int i;
			for(i = 0; i < mPreds.length; i ++) {
				if(mPreds[i].evaluate(tuple)) {
					return tuple;
				}
			}
		}
		throw new IllegalStateException();
	}

} // public class Selection extends Iterator

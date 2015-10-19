package relop;

import global.Minibase;
import global.RID;
import global.SearchKey;
import heap.HFPage;
import heap.HeapFile;
import index.HashIndex;
import index.HashScan;
import bufmgr.BufMgr;

/**
 * Wrapper for hash scan, an index access method.
 */
public class KeyScan extends Iterator {
	HashScan mScan ;
	HashIndex mIndex;
	SearchKey mKey;
	HeapFile file;
	/**
	 * Constructs an index scan, given the hash index and schema.
	 */
	public KeyScan(Schema schema, HashIndex index, SearchKey key, HeapFile file) {
		this.schema = schema;
		mIndex = index;
		mKey = key;
		this.file=file;
		restart();
	}

	/**
	 * Gives a one-line explaination of the iterator, repeats the call on any
	 * child iterators, and increases the indent depth along the way.
	 */
	public void explain(int depth) {
		indent(depth);
		System.out.println("Keyscan");
	}

	/**
	 * Restarts the iterator, i.e. as if it were just constructed.
	 */
	public void restart() {
		mScan = mIndex.openScan(mKey);
	}

	/**
	 * Returns true if the iterator is open; false otherwise.
	 */
	public boolean isOpen() {
		return (mScan != null);
	}

	/**
	 * Closes the iterator, releasing any resources (i.e. pinned pages).
	 */
	public void close() {
		mScan.close();
		mScan = null;
	}

	/**
	 * Returns true if there are more tuples, false otherwise.
	 */
	public boolean hasNext() {
		return mScan.hasNext();
	}

	/**
	 * Gets the next tuple in the iteration.
	 * 
	 * @throws IllegalStateException if no more tuples
	 */
	public Tuple getNext() {
		RID rid = mScan.getNext();
		byte[] data = file.selectRecord(rid);
		return new Tuple(schema, data);
	}

} // public class KeyScan extends Iterator

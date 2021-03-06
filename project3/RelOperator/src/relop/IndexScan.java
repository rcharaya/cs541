package relop;

import global.RID;
import global.SearchKey;
import heap.HeapFile;
import index.BucketScan;
import index.HashIndex;

/**
 * Wrapper for bucket scan, an index access method.
 */
public class IndexScan extends Iterator {
	BucketScan mScan;
	HashIndex mIndex;
	HeapFile file;

	/**
	 * Constructs an index scan, given the hash index and schema.
	 */
	public IndexScan(Schema schema, HashIndex index, HeapFile file) {
		this.schema = schema;
		this.file=file;
		mIndex = index;
		mScan = index.openScan();
	}

	/**
	 * Gives a one-line explaination of the iterator, repeats the call on any
	 * child iterators, and increases the indent depth along the way.
	 */
	public void explain(int depth) {
		indent(depth);
		System.out.println("IndexScan");
	}

	/**
	 * Restarts the iterator, i.e. as if it were just constructed.
	 */
	public void restart() {
		mScan = mIndex.openScan();
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
	 * @throws IllegalStateException
	 *             if no more tuples
	 */
	public Tuple getNext() {
		RID rid = mScan.getNext();
		byte[] data = file.selectRecord(rid);
		return new Tuple(schema, data);
	}

	/**
	 * Gets the key of the last tuple returned.
	 */
	public SearchKey getLastKey() {
		return mScan.getLastKey();
	}

	/**
	 * Returns the hash value for the bucket containing the next tuple, or
	 * maximum number of buckets if none.
	 */
	public int getNextHash() {
		return mScan.getNextHash();
	}

} // public class IndexScan extends Iterator

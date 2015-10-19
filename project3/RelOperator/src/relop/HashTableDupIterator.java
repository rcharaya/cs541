package relop;

import global.SearchKey;

public class HashTableDupIterator extends Iterator {
	int mIndex;
	Tuple[] mTuples;
	HashTableDup mTable;
	SearchKey mKey;
	
	public HashTableDupIterator(HashTableDup table, SearchKey key) {
		mKey = key;
		mTable = table;
		restart();
	}

	@Override
	public void explain(int depth) {
		// TODO Auto-generated method stub

	}

	@Override
	public void restart() {
		mIndex = 0;
		mTuples = mTable.getAll(mKey);
	}

	@Override
	public boolean isOpen() {
		return (mTuples != null);
	}

	@Override
	public void close() {
		mTuples =  null;
	}

	@Override
	public boolean hasNext() {
		return (mIndex < mTuples.length);
	}

	@Override
	public Tuple getNext() {
		return mTuples[mIndex++];
	}

}

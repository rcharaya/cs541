package relop;

import global.AttrOperator;
import global.AttrType;
import global.RID;
import global.SearchKey;
import heap.HeapFile;
import index.HashIndex;

/*
	Design: Iterate over f1, f2, persist data in heap files and build index on the fly.
	Open index scan on index f1 and key scan on index f2, build a hash table to compare 
*/
public class HashJoin extends Iterator {

	private Iterator left;

	private Iterator right;

	private HeapFile l_file;

	private HeapFile r_file;

	private HashIndex l_index;

	private HashIndex r_index;

	private int i1;

	private int i2;

	private IndexScan outer;

	HashTableDupIterator tableIterator;
	
	private KeyScan keyScan;
	
	// pre-fetched tuple
	private Tuple nextTuple; 
	
	Tuple rightTuple;
	
	HashTableDup hashtable;
	
	public HashJoin(Iterator f1,
			Iterator f2, int i1, int i2){
		left = f1;
		right = f2;
		this.i1=i1;
		this.i2=i2;
		
		this.schema = Schema.join(left.schema, right.schema);
		
		l_file = new HeapFile(null);
		l_index = new HashIndex(null);
		r_file = new HeapFile(null);
		r_index = new HashIndex(null);
		while(left.hasNext()){
			Tuple tuple = left.getNext();
			RID rid = l_file.insertRecord(tuple.getData());
			l_index.insertEntry(new SearchKey(tuple.getField(i1)), rid);
		}
		
		while(right.hasNext()){
			Tuple tuple = right.getNext();
			RID rid = r_file.insertRecord(tuple.getData());
			r_index.insertEntry(new SearchKey(tuple.getField(i2)), rid);
		} 
		outer = new IndexScan(left.schema, l_index, l_file);
	}

	
	@Override
	public void explain(int depth) {
		  indent(depth);
		  System.out.println("HashJoin");
		  left.explain(depth+1);
		  right.explain(depth+1);
	}

	@Override
	public void restart() {
		outer.restart();
		rightTuple = null;
		keyScan = null;
		hashtable = null;
		tableIterator = null;
	}

	@Override
	public boolean isOpen() {
		if(outer!=null)
			return true;
		return false;
	}

	@Override
	public void close() {
		outer.close();
		outer=null;
		hashtable = null;
		if(keyScan != null) {
			keyScan.close();
			keyScan = null;
		}
		if(tableIterator != null) {
			tableIterator.close();
			tableIterator = null;
		}
		rightTuple = null;
	}
	
	private SearchKey instantiateHashTable () {
		SearchKey key = null;
		hashtable = new HashTableDup();
		int currentHash = outer.getNextHash();
		while(outer.hasNext() && outer.getNextHash()==currentHash){
			Tuple tuple = outer.getNext();
			key = new SearchKey(tuple.getField(i1));
			hashtable.add(key, tuple);
		}
		return key;
	}
	
	@Override
	public boolean hasNext() {
		if(nextTuple != null){
			return true;
		}
		
		if(tableIterator != null && tableIterator.hasNext()) {
			Tuple leftTuple = tableIterator.getNext();
			if(leftTuple.getField(i1).equals(rightTuple.getField(i2))){
				nextTuple = Tuple.join(leftTuple, rightTuple, this.schema);
				return true;
			}
			return this.hasNext();
		}
		
		if(keyScan != null && keyScan.hasNext()) {
			rightTuple = keyScan.getNext();
			SearchKey key = new SearchKey(rightTuple.getField(i2));
			tableIterator = new HashTableDupIterator(hashtable, key);
			return this.hasNext();
		}
		
		SearchKey key = instantiateHashTable();
		if(key == null) {
			return false;
		}
		keyScan = new KeyScan(right.schema, r_index, key, r_file);
		tableIterator = null;
		return this.hasNext();
	}
		
	@Override
	public Tuple getNext() {
		Tuple tuple = nextTuple;
		nextTuple = null;
		return tuple;
	}

}
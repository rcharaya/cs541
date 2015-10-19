package query;

import global.Minibase;
import global.RID;
import global.SearchKey;
import heap.HeapFile;
import heap.HeapScan;
import index.HashIndex;
import parser.AST_Delete;
import relop.Predicate;
import relop.Schema;
import relop.Tuple;

/**
 * Execution plan for deleting tuples.
 */
class Delete implements Plan {
	String tableName;
	Predicate[][] predicates;
	Schema schema;
	/**
	 * Optimizes the plan, given the parsed query.
	 * 
	 * @throws QueryException
	 *             if table doesn't exist or predicates are invalid
	 */
	public Delete(AST_Delete tree) throws QueryException {
		tableName = tree.getFileName();
		QueryCheck.tableExists(tableName);
		schema = Minibase.SystemCatalog.getSchema(tableName);
		predicates = tree.getPredicates();
		QueryCheck.predicates(schema, predicates);
	} // public Delete(AST_Delete tree) throws QueryException

	/**
	 * Executes the plan and prints applicable output.
	 */
	public void execute() {
		int rowCount = 0;
		HeapFile hFile = new HeapFile(tableName);
		HeapScan hScan = hFile.openScan();
		
		IndexDesc[] inds = Minibase.SystemCatalog.getIndexes(tableName);
		while(hScan.hasNext()) {
			RID rid = new RID();
			byte[] data = hScan.getNext(rid);
			Tuple tuple = new Tuple(schema, data);
			if(PredicateCheck.check(predicates, tuple)) {
				rowCount ++;
				// Delete from indexes
				for(IndexDesc ind : inds) {
					HashIndex hIndex = new HashIndex(ind.indexName);
					SearchKey key = new SearchKey(tuple.getField(ind.columnName));
					hIndex.deleteEntry(key, rid);
				}
				hFile.deleteRecord(rid);
			}
		}
		Minibase.SystemCatalog.delete(tableName, rowCount);
		// print the output message
//		System.out.println("Pin Count:"+(100 - Minibase.BufferManager.getNumUnpinned()) + " "+this.getClass().getCanonicalName());
		System.out.println(rowCount + " rows deleted.");
		hScan.close();
	} // public void execute()

} // class Delete implements Plan

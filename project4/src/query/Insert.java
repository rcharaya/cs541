package query;

import global.Minibase;
import global.RID;
import global.SearchKey;
import heap.HeapFile;
import index.HashIndex;
import parser.AST_Insert;
import relop.Schema;
import relop.Tuple;

/**
 * Execution plan for inserting tuples.
 */
class Insert implements Plan {
	String tableName;
	Tuple tuple;
	Schema schema;
	/**
	 * Optimizes the plan, given the parsed query.
	 * 
	 * @throws QueryException
	 *             if table doesn't exists or values are invalid
	 */
	public Insert(AST_Insert tree) throws QueryException {
		tableName = tree.getFileName();
		QueryCheck.tableExists(tableName);
		schema = Minibase.SystemCatalog.getSchema(tableName);
		Object[] values= tree.getValues();
		QueryCheck.insertValues(schema, values);
		tuple = new Tuple(schema, values);
	} // public Insert(AST_Insert tree) throws QueryException

	/**
	 * Executes the plan and prints applicable output.
	 */
	public void execute() {
		HeapFile hFile = new HeapFile(tableName);
		RID rid = hFile.insertRecord(tuple.getData());
		
		IndexDesc[] inds = Minibase.SystemCatalog.getIndexes(tableName);
	    for (IndexDesc ind : inds) {
	      HashIndex hIndex = new HashIndex(ind.indexName);
	      SearchKey key = new SearchKey(tuple.getField(ind.columnName));
	      hIndex.insertEntry(key, rid);
	    }
	    
	    Minibase.SystemCatalog.insert(tableName);
		// print the output message
//		System.out.println("Pin Count:"+(100 - Minibase.BufferManager.getNumUnpinned()) + " "+this.getClass().getCanonicalName());
		System.out.println("1 row inserted in " + tableName );
	} // public void execute()

} // class Insert implements Plan

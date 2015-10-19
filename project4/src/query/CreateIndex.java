package query;

import global.Minibase;
import global.RID;
import global.SearchKey;
import heap.HeapFile;
import heap.HeapScan;
import index.HashIndex;
import parser.AST_CreateIndex;
import relop.Schema;
import relop.Tuple;

/**
 * Execution plan for creating indexes.
 */
class CreateIndex implements Plan {
	String fileName;
	String tableName;
	String colName;
	Schema schema;
	/**
	 * Optimizes the plan, given the parsed query.
	 * 
	 * @throws QueryException
	 *             if index already exists or table/column invalid
	 */
	public CreateIndex(AST_CreateIndex tree) throws QueryException {
		fileName = tree.getFileName();
		tableName = tree.getIxTable();
		colName = tree.getIxColumn();
		QueryCheck.fileNotExists(fileName);
		QueryCheck.tableExists(tableName);
		schema = Minibase.SystemCatalog.getSchema(tableName);
		QueryCheck.columnExists(schema, colName);
	} // public CreateIndex(AST_CreateIndex tree) throws QueryException

	/**
	 * Executes the plan and prints applicable output.
	 */
	public void execute() {
		HashIndex hIndex = new HashIndex(fileName);
		HeapFile hFile = new HeapFile(tableName);
		HeapScan hScan = hFile.openScan();
		while(hScan.hasNext()) {
			RID rid = new RID();
			byte[] data = hScan.getNext(rid);
			Tuple tuple = new Tuple(schema, data);
			SearchKey key = new SearchKey(tuple.getField(colName));
			hIndex.insertEntry(key, rid);
		}
		hScan.close();
		
	    // add the schema to the catalog
	    Minibase.SystemCatalog.createIndex(fileName, tableName, colName);

	    // print the output message
	    System.out.println("Index created.");

	} // public void execute()

} // class CreateIndex implements Plan

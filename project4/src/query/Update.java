package query;

import global.Minibase;
import global.RID;
import global.SearchKey;
import heap.HeapFile;
import heap.HeapScan;
import index.HashIndex;
import parser.AST_Update;
import relop.Predicate;
import relop.Schema;
import relop.Tuple;

/**
 * Execution plan for updating tuples.
 */
class Update implements Plan {
	String tableName;
	int[] fieldNos;
	Schema schema;
	Object[] values;
	Predicate[][] predicates;

	/**
	 * Optimizes the plan, given the parsed query.
	 * 
	 * @throws QueryException
	 *             if invalid column names, values, or predicates
	 */
	public Update(AST_Update tree) throws QueryException {
		tableName = tree.getFileName();
		QueryCheck.tableExists(tableName);
		schema = Minibase.SystemCatalog.getSchema(tableName);
		String[] columns = tree.getColumns();
		QueryCheck.updateFields(schema, columns);
		
		fieldNos = new int[columns.length];
		for (int i = 0; i < columns.length; i++) {
			String column = columns[i];
			fieldNos[i] = schema.fieldNumber(column);
		}
		values = tree.getValues();
		QueryCheck.updateValues(schema, fieldNos, values);
		predicates = tree.getPredicates();
		QueryCheck.predicates(schema, predicates);
	} // public Update(AST_Update tree) throws QueryException

	/**
	 * Executes the plan and prints applicable output.
	 */
	public void execute() {
		int rowCount = 0;
		HeapFile hFile = new HeapFile(tableName);
		HeapScan hScan = hFile.openScan();
		
		IndexDesc[] inds = Minibase.SystemCatalog.getIndexes(tableName, schema,
				fieldNos);
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
				
				for (int i = 0; i < fieldNos.length; i++) {
					tuple.setField(fieldNos[i], values[i]);
				}
				hFile.updateRecord(rid, tuple.getData());
				
				// Re-insert into indexes
				for(IndexDesc ind : inds) {
					HashIndex hIndex = new HashIndex(ind.indexName);
					SearchKey key = new SearchKey(tuple.getField(ind.columnName));
					hIndex.insertEntry(key, rid);
				}
			}
		}
		hScan.close();
//		System.out.println("Pin Count:"+(100 - Minibase.BufferManager.getNumUnpinned()) + " "+this.getClass().getCanonicalName());
		// print the output message
		System.out.println(rowCount + " rows updated.");

	} // public void execute()

} // class Update implements Plan

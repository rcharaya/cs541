package query;

import global.AttrType;
import global.Minibase;
import heap.HeapFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import parser.AST_Select;
import relop.FileScan;
import relop.Iterator;
import relop.Predicate;
import relop.Projection;
import relop.Schema;
import relop.Selection;
import relop.SimpleJoin;

/**
 * Execution plan for selecting tuples.
 */
class Select implements Plan {

	private Iterator iter;

	/**
	 * Optimizes the plan, given the parsed query.
	 * 
	 * @throws QueryException if validation fails
	 */
	public Select(AST_Select tree) throws QueryException {
		validate(tree);
		String[] tables = tree.getTables();
		List<TableInfo> tableInfos = sortTables(tables);
		Map<String, String> fieldToTableMap = parseSchemas(tables);
		Predicate[][] predicates = tree.getPredicates();
		MetaInfo info = mapPredicates(predicates, fieldToTableMap);
		for(int i=0;i<tableInfos.size();i++){
//			System.out.println(tableInfos.get(i).name);
			Schema schema = Minibase.SystemCatalog.getSchema(tableInfos.get(i).name);
			HeapFile file = new HeapFile(tableInfos.get(i).name);
			Iterator fileScan = new FileScan(schema, file);
			if(info.map.containsKey(tableInfos.get(i).name)){
				Set<Integer> indices = info.map.get(tableInfos.get(i).name);
				for(Integer index : indices){
					fileScan = new Selection(fileScan, predicates[index]);
				}
			}
			if(i==0){
				iter = fileScan;
			} else{
				Predicate[] preds = new Predicate[0];
				iter = new SimpleJoin(iter, fileScan, preds);
			}
		}
		for(int i=0;i<predicates.length;i++){
			if(!info.flags[i])
				iter = new Selection(iter, predicates[i]);
		}
		String[] columns = tree.getColumns();
		if(columns.length!=0)
		{		
			Integer[] fields = new Integer[columns.length];
			for(int i=0;i<columns.length;i++){
				Schema schema = iter.getSchema();
				fields[i]=schema.fieldNumber(columns[i]);
			}
			iter = new Projection(iter, fields);
		}
	} // public Select(AST_Select tree) throws QueryException

	private Map<String, String> parseSchemas(String[] tables){
		Map<String, String> map = new HashMap<String, String>();
		for(int i=0;i<tables.length;i++){
			Schema schema =  Minibase.SystemCatalog.getSchema(tables[i]);
			for(int j=0; j<schema.getCount(); j++){
				map.put(schema.fieldName(j), tables[i]);
			}
		}
		return map;
	}
	
	
	private MetaInfo mapPredicates(Predicate[][] predicates, Map<String, String> fieldToTableMap){
		MetaInfo info = new MetaInfo();
		info.flags = new boolean[predicates.length];
		info.map = new HashMap<String, Set<Integer>>();
		List<Integer> types = Arrays.asList(AttrType.INTEGER,AttrType.FLOAT,AttrType.STRING);
		for(int i=0;i<predicates.length;i++){
			String table = null;
			int j=0;
			for(j=0;j<predicates[i].length;j++){
				Integer rtype = predicates[i][j].getRtype();
				String field = predicates[i][j].getLeft().toString();
				table = table==null ? fieldToTableMap.get(field).toString() : table;
				if(!types.contains(rtype) || !table.equals(fieldToTableMap.get(field).toString())){
					break;
				}
			}
			if(j==predicates[i].length){
				if(info.map.get(table)==null)
					info.map.put(table, new HashSet<Integer>());
				info.map.get(table).add(i);
				info.flags[i]=true;
			}
		}
		return info;
	}
	
	List<TableInfo> sortTables(String[] tables){
		List<TableInfo> tableInfos = new ArrayList<Select.TableInfo>();
		for(int i=0;i<tables.length;i++){
			tableInfos.add(new TableInfo(tables[i], Minibase.SystemCatalog.recCount(tables[i], true)));
		}
		Collections.sort(tableInfos);
		return tableInfos;
	}
	
	void validate(AST_Select tree) throws QueryException {
		String[] tables = tree.getTables();
		QueryCheck.tableExists(tables[0]);
		Schema predictedSchema = Minibase.SystemCatalog.getSchema(tables[0]);
		for(int i=1;i<tables.length;i++){
			QueryCheck.tableExists(tables[i]);
			Schema schema = Minibase.SystemCatalog.getSchema(tables[i]);
			predictedSchema = Schema.join(predictedSchema, schema);
		}

		Predicate[][] predicates = tree.getPredicates();
		for(int i=0;i<predicates.length;i++){
			QueryCheck.predicates(predictedSchema, predicates);
		}
		String[] columns = tree.getColumns();
		for(int i=0;i<columns.length;i++){
			QueryCheck.columnExists(predictedSchema, columns[i]);
		}
	}

	/**
	 * Executes the plan and prints applicable output.
	 */
	public void execute() {
//		iter.explain(0);
		iter.execute();
//		System.out.println("Pin Count:"+(100 - Minibase.BufferManager.getNumUnpinned())+ " "+this.getClass().getCanonicalName());
		iter.close();
		// print the output message
		System.out.println("0 rows affected.");

	} // public void execute()

	private class MetaInfo{
		boolean[] flags;
		Map<String, Set<Integer>> map; 
	}
	
	private class TableInfo implements Comparable<TableInfo>{
		String name;
		Integer size;
		public TableInfo(String name, Integer size) {
			this.name=name;
			this.size=size;
		}
		@Override
		public int compareTo(TableInfo o) {
			return this.size.compareTo(o.size);
		}
		@Override
		public String toString() {
			return size.toString();
		}
	}

} // class Select implements Plan

package query;

import global.AttrType;
import global.Minibase;
import parser.AST_Describe;
import relop.Schema;

/**
 * Execution plan for describing tables.
 */
class Describe implements Plan {

	private Schema schema;
  /**
   * Optimizes the plan, given the parsed query.
   * 
   * @throws QueryException if table doesn't exist
   */
  public Describe(AST_Describe tree) throws QueryException {
		String tableName = tree.getFileName();
		QueryCheck.tableExists(tableName);
		schema = Minibase.SystemCatalog.getSchema(tableName);
  } // public Describe(AST_Describe tree) throws QueryException

  /**
   * Executes the plan and prints applicable output.
   */
  public void execute() {
	  for(int i=0;i<schema.getCount();i++){
		  System.out.println(schema.fieldName(i)+"\t\t"+AttrType.toString(schema.fieldType(i)));
	  }
    // print the output message
    System.out.println("0 rows affected");

  } // public void execute()

} // class Describe implements Plan

package tests;

import global.AttrOperator;
import global.AttrType;
import global.RID;
import heap.HeapFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import relop.FileScan;
import relop.Predicate;
import relop.Projection;
import relop.Schema;
import relop.Selection;
import relop.SimpleJoin;
import relop.Tuple;

// YOUR CODE FOR PART3 SHOULD GO HERE.

public class QEPTest extends TestDriver {
	/** The display name of the test suite. */
	private static final String TEST_NAME = "QEP tests";

	/** Employees table schema. */
	private Schema s_employees;
	private HeapFile employeeFile;

	/** Department table schema. */
	private Schema s_departments;
	private HeapFile departmentFile;

	
	public static void main(String[] args) throws IOException {
		if(args.length != 1) {
			System.err.println("Usage: java QEPTest <folder>" );
			return;
		}
		String dir = args[0];
		
		// create a clean Minibase instance
		QEPTest qepTest = new QEPTest();
		qepTest.create_minibase();
		
		qepTest.readEmps(dir);
		qepTest.readDepts(dir);
		
		// run all the test cases
		System.out.println("\n" + "Running " + TEST_NAME + "...");
		boolean status = PASS;
		status &= qepTest.test0();
		status &= qepTest.test1();
		status &= qepTest.test2();
		status &= qepTest.test3();
		status &= qepTest.test4();
		status &= qepTest.test5();
		status &= qepTest.test6();
		status &= qepTest.test7();
		status &= qepTest.test8();

		// display the final results
		System.out.println();
		if (status != PASS) {
			System.out
					.println("Error(s) encountered during " + TEST_NAME + ".");
		} else {
			System.out.println("All " + TEST_NAME
					+ " completed; verify output for correctness.");
		}
	}
	
	private boolean test0() {
		try {
			System.out.println("\nTest 1: Primative relational operators");
			FileScan employeeScan = new FileScan(s_employees, employeeFile);
			employeeScan.execute();
			
			FileScan departmentScan = new FileScan(s_departments, departmentFile);
			departmentScan.execute();
			return PASS;
		} catch(Exception e) {
			e.printStackTrace();
			return FAIL;
		}
	}
	
	private void readEmps(String dir) throws NumberFormatException, IOException {
		// initialize schema for the "Drivers" table
		s_employees = new Schema(5);
		s_employees.initField(0, AttrType.INTEGER, 4, "EmpId");
		s_employees.initField(1, AttrType.STRING, 20, "Name");
		s_employees.initField(2, AttrType.INTEGER, 4, "Age");
		s_employees.initField(3, AttrType.INTEGER, 4, "Salary");
		s_employees.initField(4, AttrType.INTEGER, 4, "DeptID");

		BufferedReader empReader = new BufferedReader(new FileReader(dir
				+ File.separator + "Employee.txt"));
		String line;
		employeeFile = new HeapFile("Employee");
		// Skip first line
		line = empReader.readLine();
		while((line = empReader.readLine()) != null) {
			String[] tokens = line.split(",");
			Tuple tuple = new Tuple(s_employees);
			tuple.setIntFld(0, Integer.parseInt(tokens[0].trim()));
			tuple.setStringFld(1, tokens[1]);
			tuple.setIntFld(2, Integer.parseInt(tokens[2].trim()));
			tuple.setIntFld(3, Integer.parseInt(tokens[3].trim()));
			tuple.setIntFld(4, Integer.parseInt(tokens[4].trim()));

			// insert the tuple in the file and index
			RID rid = employeeFile.insertRecord(tuple.getData());
		}
		empReader.close();
	}
	
	private void readDepts(String dir) throws NumberFormatException, IOException {
		// initialize schema for the "Rides" table
		s_departments = new Schema(4);
		s_departments.initField(0, AttrType.INTEGER, 4, "DeptId");
		s_departments.initField(1, AttrType.STRING, 20, "Name");
		s_departments.initField(2, AttrType.INTEGER, 4, "MinSalary");
		s_departments.initField(3, AttrType.INTEGER, 4, "MaxSalary");

		BufferedReader deptReader = new BufferedReader(new FileReader(dir
				+ File.separator + "Department.txt"));
		String line;
		departmentFile = new HeapFile("Department");
		// Skip first line
		line = deptReader.readLine();
		while((line = deptReader.readLine()) != null) {
			String[] tokens = line.split(",");
			Tuple tuple = new Tuple(s_employees);
			tuple.setIntFld(0, Integer.parseInt(tokens[0].trim()));
			tuple.setStringFld(1, tokens[1]);
			tuple.setIntFld(2, Integer.parseInt(tokens[2].trim()));
			tuple.setIntFld(3, Integer.parseInt(tokens[3].trim()));

			// insert the tuple in the file and index
			RID rid = departmentFile.insertRecord(tuple.getData());
		}
		deptReader.close();
	}
	
	private boolean test1() {
		System.out.println("\n ~> Display for each employee his Name and Salary\n");
		FileScan scan = new FileScan(s_employees, employeeFile);
		Projection pro = new Projection(scan, 1, 3);
		pro.execute();
		return PASS;
	}
	
	private boolean test2() {
		System.out.println("\n ~> 2. Display the Name for the departments with "
				+ "MinSalary = 1000\n");
		FileScan scan = new FileScan(s_departments, departmentFile);
		Predicate pred = 
				new Predicate(AttrOperator.EQ, AttrType.FIELDNO, 2, AttrType.INTEGER,
						1000);
		
		Selection sel = new Selection(scan, pred);
		Projection pro = new Projection(sel, 1);
		pro.execute();
		return PASS;
	}
	
	private boolean test3() {
		System.out.println("\n ~> 3. Display the Name for the departments with "
				+ "MinSalary = MaxSalary\n");
		FileScan scan = new FileScan(s_departments, departmentFile);
		Predicate pred = 
				new Predicate(AttrOperator.EQ, AttrType.FIELDNO, 2, AttrType.FIELDNO, 3);
		
		Selection sel = new Selection(scan, pred);
		Projection pro = new Projection(sel, 1);
		pro.execute();
		return PASS;
	}
	
	private boolean test4() {
		System.out.println("\n ~> 4. Display the Name for employees whose "
				+ "Age > 30 and Salary < 1000\n");
		FileScan scan = new FileScan(s_employees, employeeFile);
		Selection agesel = new Selection(scan, new Predicate(AttrOperator.GT,
				AttrType.FIELDNO, 2, AttrType.INTEGER, 30));
		Selection salsel = new Selection(agesel, new Predicate(AttrOperator.LT,
				AttrType.FIELDNO, 3, AttrType.INTEGER, 1000));
		Projection pro = new Projection(salsel, 1);
		pro.execute();
		return PASS;
	}
	
	private boolean test5() {
		System.out.println("\n ~> 5. For each employee, display his Salary and "
				+ "the Name of his department\n");
		Predicate pred = new Predicate(AttrOperator.EQ, AttrType.FIELDNO, 4,
				AttrType.FIELDNO, 5);
		SimpleJoin join = new SimpleJoin(
				new FileScan(s_employees, employeeFile), new FileScan(
						s_departments, departmentFile), pred);
		Projection proj = new Projection(join, 1, 3, 6);
		proj.execute();
		return PASS;
	}
	
	private boolean test6() {
		System.out.println("\n ~> 6. Display the Name and Salary for employees "
				+ "who work in the department that has DeptId = 3\n");
		Predicate selPred = new Predicate(AttrOperator.EQ, AttrType.FIELDNO, 4,
				AttrType.INTEGER, 3);
		Selection sel = new Selection(new FileScan(s_employees,
				employeeFile), selPred);
		Projection proj = new Projection(sel, 1, 3);
		proj.execute();
		return PASS;
	}
	
	private boolean test7() {
		System.out.println("\n ~> 7. Display the Salary for each employee who "
				+ "works in a department that has MaxSalary > 100000\n");
		Predicate joinPred = new Predicate(AttrOperator.EQ, AttrType.FIELDNO, 4,
				AttrType.FIELDNO, 5);
		Predicate selPred = new Predicate(AttrOperator.GT, AttrType.FIELDNO, 3,
				AttrType.INTEGER, 100000);
		Selection sel = new Selection(new FileScan(s_departments,
				departmentFile), selPred);
		SimpleJoin join = new SimpleJoin(
				new FileScan(s_employees, employeeFile), sel, joinPred);
		Projection proj = new Projection(join, 1, 3);
		proj.execute();
		return PASS;
	}
	
	private boolean test8() {
		System.out.println("\n ~> 8. Display the Name for each employee whose "
				+ "Salary is less than the MinSalary of his department\n");
		Predicate joinPred = new Predicate(AttrOperator.EQ, AttrType.FIELDNO, 4,
						AttrType.FIELDNO, 5);
		SimpleJoin join = new SimpleJoin(
				new FileScan(s_employees, employeeFile), new FileScan(
						s_departments, departmentFile), joinPred);
		Selection sel = new Selection(join, new Predicate(AttrOperator.LT,
				AttrType.FIELDNO, 3, AttrType.FIELDNO, 7));
		Projection proj = new Projection(sel, 1, 3, 7);
		proj.execute();
		return PASS;
	}
}

-- rem CS 541 SQL Project 12
-- rem Abhilash Jindal

set serveroutput on size 30000;
CREATE OR REPLACE VIEW school_student 
	AS SELECT sid, schid 
	from school
	join student on student.schoolid = school.schid;


--CREATE OR REPLACE FUNCTION companies_school (p_schid in number)
--return school.name%type
--is 
--top_sch_interned school.name%type;
--BEGIN
--	select sch.name into top_sch_interned from
--		intern_student v join school sch
--		on sch.schid=v.schoolid
--		where v.cmpid=cmpid
--		and rownum =1 
--		order by sch.rank;
--
--		return top_sch_interned;
--END	companies_school;
--/

CREATE OR REPLACE FUNCTION num_interned (p_schid in number)
return number
is 
total_interned number;
BEGIN
	select count(*) into total_interned from
		school_student 		
		join (select distinct(sid) from intern) dis_intern
		on school_student.sid = dis_intern.sid
		where school_student.schid = p_schid;
	return total_interned;
END	num_interned;
/

CREATE OR REPLACE PROCEDURE pro_school_report
AS
cursor companies(q_schid in number) is
	(select company.cmpid, company.title, count(*) num_students from
		school_student 
		join (select distinct sid, cmpid from intern) dis_intern
		on school_student.sid = dis_intern.sid
		join company 
		on company.cmpid = dis_intern.cmpid
		where school_student.schid = q_schid
		group by company.cmpid, company.title);
cursor schools is
	(select schid, school.name, rank, count(schid) cnt
		from school
		join student on student.schoolid = school.schid
	group by schid, school.name, rank);
BEGIN
	for s in schools loop
		dbms_output.put_line('SchoolId: ' || s.schid);
		dbms_output.put_line('SchoolName: ' || s.name);
		dbms_output.put_line('TotalNumOfStudents NumOfStudentsInterned SchoolRank');
		dbms_output.put_line('------------------ --------------------- ----------');
		dbms_output.put_line(rpad(to_char(s.cnt, 9999), 19)||rpad(to_char(num_interned(s.schid), 9999), 26)||s.rank);
		dbms_output.put_line('CompaniesInterned');
		dbms_output.put_line('CompanyID CompanyName NumOfStudents');
		dbms_output.put_line('--------- ----------- -------------');
		for c in companies(s.schid) loop
			dbms_output.put_line(rpad(to_char(c.cmpid, 9999), 11)||rpad(c.title, 12)||c.num_students);
		end loop;
		dbms_output.put_line(chr(10));
		dbms_output.new_line();
	end loop;
END pro_school_report;
/

BEGIN
	pro_school_report;
END;
/

show error;

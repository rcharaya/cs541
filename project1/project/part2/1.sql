-- rem CS 541 SQL Project 12
-- rem Abhilash Jindal

set serveroutput on size 30000;
CREATE OR REPLACE VIEW intern_student AS SELECT i.cmpid, i.sid, (i.enddate - i.startdate) duration, s.schoolid from intern i join student s on i.sid=s.sid;

CREATE OR REPLACE FUNCTION find_top_school (cmpid in number)
return school.name%type
is 
top_sch_interned school.name%type;
BEGIN
	select sch.name into top_sch_interned from
		intern_student v join school sch
		on sch.schid=v.schoolid
		where v.cmpid=cmpid
		and rownum =1 
		order by sch.rank;

		return top_sch_interned;
END	find_top_school;
/

CREATE OR REPLACE FUNCTION find_most_school (cmpid in number)
return school.name%type
is 
most_intern_school.name%type;
BEGIN
	select sch.name into most_intern_school from
		(select schoolid, rank() OVER (ORDER BY cnt DESC) rnk from
			(select v.schoolid, count(*) cnt from intern_student v 
				where v.cmpid = cmpid
				group by v.schoolid )
			) temp where temp.rnk = 1 
			join school sch 
			on temp.schoolid = sch.schid;
		return most_intern_school;
END	find_most_school;
/

create or replace function to_mmdd (interval in number)
	return varchar
	is
	r_mmdd varchar(10);
	months number;
	days number;
BEGIN
	months := trunc(interval/30);
	days := mod(interval, 30);
	r_mmdd := months || '-' || days;
	return r_mmdd;
END to_mmdd;
/

CREATE OR REPLACE PROCEDURE pro_comp_report
AS
cursor records is
		select company.cmpid, s_count, sch_count, average, title from
			(select cmpid, count(v.sid) s_count, 
				count(distinct v.schoolid) sch_count, 
				avg(v.duration) average from
				intern_student v 
				join school sch
				on v.schoolid=sch.schid
				group by cmpid) tmp
			join company on company.cmpid = tmp.cmpid;
BEGIN
	for r in records loop

		dbms_output.put_line('CompanyTitle NumOfInterns NumOfSchools'
				|| ' AvgInternDuration TopSchoolInterned SchoolWithMostIntern');
		dbms_output.put_line('------------ ------------ ------------'
				|| ' ----------------- ----------------- --------------------');
	
		dbms_output.put_line(rpad(r.title, 9)||rpad(to_char(r.s_count, 9999), 13)||
			rpad(to_char(r.sch_count, 9999), 17)|| rpad(to_mmdd(r.average), 18)|| 
			rpad(find_top_school(r.cmpid), 18) || find_top_school(r.cmpid));
	end loop;
END pro_comp_report;
/

BEGIN
	pro_comp_report;
END;
/

show error;

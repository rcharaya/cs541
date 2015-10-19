-- rem CS 541 SQL Project 11
-- rem Abhilash Jindal

select title, name from
	(select company.title, name, RANK() OVER (ORDER BY cnt desc) rnk from
		((select distinct_interns.cmpid, school.schid, count(*) cnt from
			(select distinct sid, cmpid from intern) distinct_interns
				join student on
				student.sid = distinct_interns.sid
				join school on
				student.schoolid = school.schid
				group by distinct_interns.cmpid, school.schid) sch_intern_count
			join school on
			school.schid = sch_intern_count.schid
			join company on
			sch_intern_count.cmpid = company.cmpid)
		) sch_intern_rnk where rnk = 1;

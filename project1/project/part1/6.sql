-- rem CS 541 SQL Project 11
-- rem Abhilash Jindal

select name from 
	(select name, RANK() OVER (ORDER BY cnt desc) rnk from
		(select name, count(*) cnt from
			(select distinct course.cid, school.name from
				studied
				join student on student.sid = studied.sid
				join course on studied.cid = course.cid
				join school on student.schoolid = school.schid) 
			group by name))
where rnk = 1;

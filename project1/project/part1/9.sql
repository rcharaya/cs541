-- rem CS 541 SQL Project 11
-- rem Abhilash Jindal

select course.cid from
	course 
	join studied on studied.cid = course.cid
	join student on studied.sid = student.sid
	join intern on intern.sid = student.sid
	join
		(select cmpid from
			(select cmpid, RANK() OVER (ORDER BY cnt DESC) rnk from
				(select cmpid, count(*) cnt from
					(select distinct sid, cmpid from intern)
					group by cmpid))
			where rnk = 1) highest_interns_comp
		on intern.cmpid = highest_interns_comp.cmpid;

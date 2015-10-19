-- rem CS 541 SQL Project 11
-- rem Abhilash Jindal

select s1.name, s2.name from 
	(select schoolid, cmpid from
		(select schoolid, cmpid, 
			rank() over (partition by schoolid order by num_students desc) rnk from 
			(select schoolid, cmpid, count(*) num_students from
				intern join student
				on intern.sid=student.sid
				group by (schoolid, cmpid)))
		where rnk = 1) school_top_comp1
join
	(select schoolid, cmpid from
		(select schoolid, cmpid, 
			rank() over (partition by schoolid order by num_students desc) rnk from 
			(select schoolid, cmpid, count(*) num_students from
				intern join student
				on intern.sid=student.sid
				group by (schoolid, cmpid)))
		where rnk = 1) school_top_comp2
	on school_top_comp1.cmpid = school_top_comp2.cmpid 
	and school_top_comp1.schoolid < school_top_comp2.schoolid 
	join school s1
	on school_top_comp1.schoolid = s1.schid
	join school s2
	on school_top_comp2.schoolid = s2.schid;

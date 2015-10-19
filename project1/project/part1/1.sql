-- rem CS 541 SQL Project 11
-- rem Abhilash Jindal

select name from student where student.sid = 
	(select sid from 
		(select sid, count(*) as num 
		from studied group by sid) 
	where num=1);

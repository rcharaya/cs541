-- rem CS 541 SQL Project 11
-- rem Abhilash Jindal

select name, cnt from school 
	join
	(select s1.schoolid schid, count(*) cnt from friend f
		join student s1
		on f.sid1=s1.sid
		join student s2
		on f.sid2=s2.sid and s1.schoolid=s2.schoolid
		group by s1.schoolid) same_school_friends
	on school.schid=same_school_friends.schid;

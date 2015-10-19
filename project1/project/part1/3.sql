-- rem CS 541 SQL Project 11
-- rem Abhilash Jindal

select title from company
where cmpid in (select i.cmpid from intern i
minus
(select intern.cmpid from intern
join student
on intern.sid=student.sid
and student.schoolid not in
(select schid from school
where rank<=3)));

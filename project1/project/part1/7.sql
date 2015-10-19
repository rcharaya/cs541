-- rem CS 541 SQL Project 11
-- rem Abhilash Jindal

select title, avg(duration) from 
	(select title, (intern.enddate-intern.startdate) duration from company
		join intern on
		intern.cmpid = company.cmpid
		and intern.enddate is not null)
	group by title;

-- rem CS 541 SQL Project 11
-- rem Abhilash Jindal

select sid1, sid2 from
	(
	select sid1, sid2, rank() over (order by num_friends desc) rnk from
		(select sid1, sid2, count(*) as num_friends from
			((select f1.sid1, f2.sid2 from friend f1, friend f2 
					where f1.sid2 = f2.sid1)
				union all
				(select f1.sid2 as sid1, f2.sid2 as sid2 
					from friend f1, friend f2 
					where f1.sid1=f2.sid1 and f1.sid2 < f2.sid2)
			 union all 
				 (select f1.sid1 as sid1, f2.sid1 as sid2                             
					 from friend f1, friend f2 
					 where f1.sid2=f2.sid2 and f1.sid1 < f2.sid1))
			group by sid1, sid2
		) common_friends
		where not exists
			(select * from friend 
				where friend.sid1 = common_friends.sid1 
				and friend.sid2 = common_friends.sid2)
	) 
	where rnk=1
;

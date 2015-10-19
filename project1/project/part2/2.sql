set serveroutput on size 30000;
create or replace view reco_friends AS 
	select reco_cands.sid1, reco_cands.sid2, num_mutual from
		-- common friends
		(select sid1, sid2, max(num_mutual) num_mutual from
			((select sid1, sid2, count(*) num_mutual from
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
				group by sid1, sid2)
				union all
				-- same intern
				(select i1.sid as sid1, i2.sid as sid2, 0 num_mutual from intern i1, intern i2
						where i1.sid < i2.sid and i1.cmpid = i2.cmpid))
				group by sid1, sid2) reco_cands
		where not exists
			(select * from friend
				where friend.sid1 = common_friends.sid1
				and friend.sid2 = common_friends.sid2)

create or replace function getname (p_sid in number)
	return student.name%type
	is
	r_name student.name%type;
BEGIN
	select name into r_name from student
		where student.sid = p_sid;
	return r_name;
END getname;
/

create or replace function getschoolname (p_sid in number)
	return school.name%type
	is
	r_name school.name%type;
BEGIN
	select school.name into r_name from student
		join school on school.schid = student.schoolid
		where student.sid = p_sid;
	return r_name;
END getschoolname;
/

create or replace function getsharedfriends (p_sid1 in number, p_sid2 in number)
	return number
	is
	r_mutual number;
	l_sid number;
	h_sid number;
BEGIN
	l_sid := p_sid2;
	h_sid := p_sid1;
	if p_sid1 < p_sid2 then
		l_sid := p_sid1;
		h_sid := p_sid2;
	end if;
		select num_mutual into r_mutual from reco_friends
			where reco_friends.sid1 = l_sid 
			and reco_friends.sid2 = h_sid;
	return r_mutual;
END getsharedfriends;
/

create or replace function getsharedcourses (p_sid1 in number, p_sid2 in number)
	return number
	is
	r_courses number;
BEGIN
	select count(*) into r_courses from studied s1
		join studied s2
		on s1.cid = s2.cid
		where s1.sid = p_sid1
		and s2.sid = p_sid2;
	return r_courses;
END getsharedcourses;
/

create or replace function to_mmdd (interval in number)
	return varchar
	is
	r_mmdd varchar(8);
	months number;
	days number;
BEGIN
	months := trunc(interval/30);
	days := mod(interval, 30);
	dbms_output.put_line(days);
	r_mmdd := months || '-' || days;
	return r_mmdd;
END to_mmdd;
/

create or replace procedure pro_friend_suggestion
	AS
	cursor recos(q_sid student.sid%type) is
		((select sid2 as sid from reco_friends where reco_friends.sid1 = q_sid)
			union all
			(select sid1 as sid from reco_friends where reco_friends.sid2 = q_sid));
	cursor studs is 
		select * from student order by sid;
	cursor interns(q_sid student.sid%type) is
		select intern.cmpid, title, (enddate - startdate) duration
			from intern
			join company 
			on company.cmpid = intern.cmpid
			where intern.sid = q_sid;
BEGIN
	for s in studs loop
		dbms_output.put_line('Student ID: ' || s.sid);
		dbms_output.put_line('Student Name: ' || s.name);
		for f in recos(s.sid) loop
			dbms_output.put_line('FriendID FriendName NumOfSharedFriends NumOfSharedCourses SchoolName');
			dbms_output.put_line('-------  ---------- ------------------ ------------------ ----------');
			dbms_output.put_line(rpad(to_char(f.sid, 9999), 13) || 
				rpad(getname(f.sid), 7) || 
				rpad(to_char(getsharedfriends(s.sid, f.sid),9999), 19) || 
				rpad(to_char(getsharedcourses(s.sid, f.sid), 9999), 23) || 
				getschoolname(f.sid));

			dbms_output.put_line('CompaniesInterned:');
			dbms_output.put_line('CompanyID CompanyName InternshipDuration');
			dbms_output.put_line('--------- ----------- ------------------');
			for i in interns(f.sid) loop
				dbms_output.put_line(rpad(to_char(i.cmpid, 9999), 11) || 
					rpad(i.title,11) || rpad(to_mmdd(i.duration), 8));
			end loop;
		end loop;
		dbms_output.put_line(chr(10));
	end loop;
END pro_friend_suggestion;
/

BEGIN
	pro_friend_suggestion;
END;
/

show error;

Project 4. CS 54100
===================

** Abhilash Jindal (jindal0@purdue.edu), Riya Charaya (rcharaya@purdue.edu)**

Riya implemented Update, Select and Describe. Abhilash did CreateIndex, DeleteIndex, Insert and Update.

Compiling and running
---------------------
<pre>
$ make
</pre>

SELECT Optimizations
--------------------
1. **Push Select under Join**
	Implemented pushing selects for predicates involving just that table under the joins. 
2. **Join Order**
	Changed join order sorting them according to table size. Smallest table as outer-most, largest as inner-most and so on.
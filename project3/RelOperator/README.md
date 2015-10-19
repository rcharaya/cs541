Project 3. CS 54100
===================

** Abhilash Jindal (jindal0@purdue.edu), Riya Charaya (rcharaya@purdue.edu)**

Compiling and running
---------------------
<pre>
$ make
</pre>

HashJoin Implementation
-----------------------

Outer table's `IndexScan` is used and inner table's `KeyScan` for doing `HashJoin`. For each partition in Outer, an in-memory hash table is first built. Next for each tuple found from inner table's `KeyScan`, in-memory hash table is looked up.

hasNext() is implemented as a recursive function handling three scenarios in a bottom up manner.

1. In-memory hash table entries matching with a particular key in Inner has finished
2. Inner's keyscan finished
3. Complete bucket of Outer has finished.
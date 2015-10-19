CS 54100 Project 2 
==================

Group member's contribution
---------------------------
- Abhilash Jindal
- Riya Charaya


2.1 BufferManager
-----------------

Design
------
There are 3 more major classes- `Frame`, `LIRS` and `MyHashTable` in addition to `BufMgr`. Frame class and MyHashTable closely follow the project description pdf. 

`LIRS` keeps all the Frames in a `TreeSet` sorted by their LIRS score. When a new empty frame is required the `TreeSet.last` for maximum LIRS score is returned which just takes O(1) time. LIRS maintains a virtual clock implemented via an integer counter; every time a page's pin count becomes 0, its LIRS score is updated and the virtual clock is incremented.

2.2, 2.3 HeapFile
-----------------
Design
------

Heapfile is loaded from the disk by iterating through HFPages. Addition of a new page links it to the previous HFPage. 

`O(logn)` insert, delete and update operations are ensured by keeping a `TreeSet` of (size, pageid).

`O(1)` record count lookup is ensured by keeping the value in the prevPage pointer of first page of Heapfile.

HeapScan takes the page id of first page of the heap file, keeps two variables to keep track of current record and current page. 

Contributions
-------------

Abhilash and Riya discussed and together came up with the designs for `TreeSet` based implementation for both BufferManager and HeapFile parts. Abhilash implemented the BufferManager and Riya implemented HeapFile and HeapScan. Bugs and corner cases were debugged collaboratively.


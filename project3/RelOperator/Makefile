JDKPATH = /usr
LIBPATH = lib/bufmgr.jar:lib/diskmgr.jar:lib/heap.jar:lib/index.jar
RUNLIBPATH = ../lib/bufmgr.jar:../lib/diskmgr.jar:../lib/heap.jar:../lib/index.jar

CLASSPATH = .:..:$(LIBPATH)
RUNCLASSPATH = .:${RUNLIBPATH}
BINPATH = $(JDKPATH)/bin
JAVAC = $(JDKPATH)/bin/javac 
JAVA  = $(JDKPATH)/bin/java 

PROGS = xx

all: $(PROGS)

compile:src/*/*.java
	mkdir bin
	$(JAVAC) -cp $(CLASSPATH) -d bin src/*/*.java

xx : compile
	cd bin && $(JAVA) -cp ${RUNCLASSPATH} tests.QEPTest ../src/tests/SampleData/

clean:
	rm -rf *.minibase bin

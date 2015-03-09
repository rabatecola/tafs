BUILDCLASSPATH=./spymemcached-2.10.3.jar
CLASSPATH=$(BUILDCLASSPATH):.

SOURCES=testmemcached.java testClient.java testServer.java
OBJECTS=$(SOURCES:.java=.class)

all: $(SOURCES) $(OBJECTS)

#testmemcached.class: testmemcached.java
#	javac -classpath $(BUILDCLASSPATH) testmemcached.java

%.class: %.java
	javac -classpath $(BUILDCLASSPATH) $<

run: testmemcached.class
	java -cp $(CLASSPATH):. testmemcached

clean:
	rm $(OBJECTS)

runclient:
	java -cp /Users/robert/Desktop/SCU/gitRepositories/Winter\ 2015/tafs:/Users/robert/Desktop/SCU/gitRepositories/Winter\ 2015/tafs/spymemcached-2.10.3.jar tafs.testClient

runserver:
	java -cp /Users/robert/Desktop/SCU/gitRepositories/Winter\ 2015/tafs:/Users/robert/Desktop/SCU/gitRepositories/Winter\ 2015/tafs/spymemcached-2.10.3.jar tafs.testServer

PTARGET=~/Desktop/SCU/Winter\ 2015
properjar:
	-rm $(PTARGET)/tafscc $(PTARGET)/tafsch
	-mv $(PTARGET)/tafsch.jar $(PTARGET)/tafscc.jar
	mv $(PTARGET)/tafscc.jar $(PTARGET)/tafscc
	ln $(PTARGET)/tafscc $(PTARGET)/tafsch

runcc:
	java -cp $(PTARGET)/tafscc tafs
	
runch:
	java -cp $(PTARGET)/tafsch tafs

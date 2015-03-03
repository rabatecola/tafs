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

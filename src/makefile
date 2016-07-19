all: User.class Client.class Server.class SessionThread.class
User.class: User.java
	javac -d . -classpath . User.java
Client.class: Client.java
	javac -d . -classpath . Client.java
SessionThread.class: SessionThread.java
	javac -d . -classpath . SessionThread.java
Server.class: Server.java
	javac -d . -classpath . Server.java
clean:
	rm -f *.class
# web-terminal
Web based terminal emulator

This simple app is based on Spring Boot 3, web sockets, and JSch ssh2 java library ( http://www.jcraft.com/jsch/).
The goal is to demonstrate these things working together, as well as the usecase: reaching out to the remote server via ssh from a web browser, with no efforts.

Live demo https://www.kriffer.io/apps/webterminal/

### Build and run

`git clone https://github.com/kriffer/web-terminal.git`

`cd web-terminal && mvn package`

`java -jar target/webterminal-0.0.1-SNAPSHOT.jar` (Please note this project uses Java 17)

 open http://localhost:8080 in browser

### To Do:
- add key ssh auth
- file transfer
- interactive mode
- tests
- ...


# DCOM-Group-Assignment (minimal RMI base)

Minimal Java RMI skeleton for HRM (register, update, apply leave, report).

Quick compile & run (javac/jar or from IDE):
1. Compile:
   - From project root:
     javac -d out src\com\bhel\hrm\**\*.java

2. Run server:
   - Start (option A) rmiregistry manually in project root:
       start rmiregistry 1099
     then:
       java -cp out com.bhel.hrm.server.ServerMain
   - Or (option B) just run ServerMain which creates registry programmatically.

3. Run client:
   java -cp out com.bhel.hrm.client.ClientMain

Enabling SSL (notes):
- This minimal base is NOT using RMI SSL socket factories by default.
- To secure communication you can:
  1. Create a keystore/truststore (keytool) for server/client.
  2. Implement RMIServerSocketFactory and RMIClientSocketFactory using SSLSocketFactory, or use third-party helpers.
  3. Set system properties before binding/lookup:
       -Djavax.net.ssl.keyStore=serverkeystore.jks -Djavax.net.ssl.keyStorePassword=changeit
       -Djavax.net.ssl.trustStore=clienttruststore.jks -Djavax.net.ssl.trustStorePassword=changeit
- References:
  - Oracle guide: "Custom Socket Factories" for RMI with SSL.

Next steps you may implement:
- Persistence (JDBC/SQLite), GUI, full report export, authentication, keystore creation script, unit tests, and a Gantt chart & report as required by the assignment.

Small Java RMI-based HRM prototype for the BHEL assignment.

Summary
- Java RMI server and clients to manage basic HR tasks (start with a minimal RMI check).
- Embedded H2 database for persistence (easy setup).
- Use IntelliJ + Maven.

Quick setup (local)
1. Clone the repo (after you create it on GitHub) and open in IntelliJ.
2. Build: mvn clean package
3. Run the server:
   - Run server.server.ServerMain from IntelliJ (or `mvn exec:java` if configured).
4. Run the client:
   - Run client.client.ClientMain from IntelliJ.

What this repo should contain
- src/main/java/common — RMI interfaces and DTOs
- src/main/java/server — RMI server implementation
- src/main/java/client — client implementations (HR and Employee)
- pom.xml — Maven build (H2 dependency included)
- docs/ — diagrams, use-cases, Gantt chart (add later)

Next development steps
- Replace the sample Hello service with HR service interfaces (registerEmployee, applyLeave, checkLeave, etc.).
- Implement persistence using H2 (JDBC + DAO layer).
- Add basic authentication and then secure RMI with SSL (keystore).
- Add unit and integration tests.

Authors and contribution
- Created by: MinoriLuna (team members should add names and workload matrix in docs/)

License
- Add a license file if required (MIT recommended for coursework).

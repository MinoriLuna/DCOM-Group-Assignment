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

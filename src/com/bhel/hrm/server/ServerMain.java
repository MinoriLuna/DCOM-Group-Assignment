package com.bhel.hrm.server;

import com.bhel.hrm.remote.HRMService;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerMain {
    public static void main(String[] args) {
        try {
            // 1. Security Setup
            // Point to our keystore file. This is like the server's ID card.
            System.setProperty("javax.net.ssl.keyStore", "keystore.jks");
            System.setProperty("javax.net.ssl.keyStorePassword", "password123");

            // 2. Start the Service
            // Create the actual object that does the work (saving employees, etc.)
            HRMService service = new HRMServiceImpl();

            // 3. Create the Registry (The "Phonebook")
            // We MUST use these SSL factories here.
            // This forces the registry to use encryption so hackers can't see the data.
            Registry reg = LocateRegistry.createRegistry(
                    1099,
                    new SslRMIClientSocketFactory(),
                    new SslRMIServerSocketFactory()
            );

            // 4. Bind the Service
            // Put our service into the registry with a name so the client can find it.
            reg.rebind("HRMService", service);

            System.out.println("HRMService bound and running on port 1099 (SSL Enabled)");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
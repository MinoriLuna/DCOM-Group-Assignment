package com.bhel.hrm.server;

import com.bhel.hrm.remote.HRMService;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerMain {
    public static void main(String[] args) {
        try {
            // 1. Load the Identity (Keystore)
            System.setProperty("javax.net.ssl.keyStore", "keystore.jks");
            System.setProperty("javax.net.ssl.keyStorePassword", "password123");

            // 2. Instantiate your service
            HRMService service = new HRMServiceImpl();

            // 3. Create the Registry using SSL Sockets
            // This forces RMI to use the keystore info loaded above
            Registry reg = LocateRegistry.createRegistry(
                    1099,
                    new SslRMIClientSocketFactory(),
                    new SslRMIServerSocketFactory()
            );

            reg.rebind("HRMService", service);

            System.out.println("HRMService bound and running on port 1099 (SSL Enabled)");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
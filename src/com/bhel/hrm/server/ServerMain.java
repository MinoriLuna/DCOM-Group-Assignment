package com.bhel.hrm.server;

import com.bhel.hrm.remote.HRMService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerMain {
    public static void main(String[] args) {
        try {
            // Set keystore/truststore system properties before starting for SSL
            System.setProperty("javax.net.ssl.keyStore", "keystore.jks");
            System.setProperty("javax.net.ssl.keyStorePassword", "password123");

            HRMService service = new HRMServiceImpl();

            // start local registry on 1099 (if not started externally)
            Registry reg = LocateRegistry.createRegistry(1099);
            reg.rebind("HRMService", service);
            System.out.println("HRMService bound and running on port 1099");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}


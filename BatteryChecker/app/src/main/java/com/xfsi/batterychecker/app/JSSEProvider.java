package com.xfsi.batterychecker.app;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Provider;

/**
 * Created by local-kieu on 6/25/14.
 */
public class JSSEProvider extends Provider{
    private static final long serialVersionUID = 1L;

    public JSSEProvider(){
        // Provider(S name, d version, S info)
        super("HarmonyJSSE", 1.0, "Harmony JSSE Provider");
        AccessController.doPrivileged(new PrivilegedAction<Void>(){
            public Void run() {
                put("SSLContext.TLS","org.apache.harmony.xnet.provider.jsse.SSLContextImpl");
                put("Alg.Alias.SSLContext.TLSv1", "TLS");
                put("KeyManagerFactory.X509",
                        "org.apache.harmony.xnet.provider.jsse.KeyManagerFactoryImpl");
                put("TrustManagerFactory.X509",
                        "org.apache.harmony.xnet.provider.jsse.TrustManagerFactoryImpl");
                return null;
            }
        });
    }
}

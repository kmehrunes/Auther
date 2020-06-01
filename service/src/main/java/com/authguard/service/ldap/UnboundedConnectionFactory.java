package com.authguard.service.ldap;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.util.Optional;

public class UnboundedConnectionFactory {
    private static final int DEFAULT_PORT = 389;
    private static final int DEFAULT_SECURE_PORT = 636;

    private String host;
    private Integer port;
    private String admin;
    private String adminPassword;
    private boolean secure;

    public UnboundedConnectionFactory(final String host, final Integer port) {
        this.host = host;
        this.port = port;
    }

    public UnboundedConnectionFactory(final String host) {
        this.host = host;
    }

    public UnboundedConnectionFactory withAdmin(final String admin, final String adminPassword) {
        this.admin = admin;
        this.adminPassword = adminPassword;

        return this;
    }

    public UnboundedConnectionFactory useSecureConnection(final boolean secure) {
        this.secure = secure;
        return this;
    }

    public LDAPConnection create() throws LDAPException {
        final int port = Optional.ofNullable(this.port)
                .orElseGet(() -> this.secure ? DEFAULT_SECURE_PORT : DEFAULT_PORT);

        final SocketFactory socketFactory = secure ? SSLSocketFactory.getDefault(): SocketFactory.getDefault();

        if (admin == null) {
            return new LDAPConnection(socketFactory, host, port);
        } else {
            return new LDAPConnection(socketFactory, host, port, admin, adminPassword);
        }
    }
}

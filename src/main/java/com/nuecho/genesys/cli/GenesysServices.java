package com.nuecho.genesys.cli;

import com.genesyslab.platform.applicationblocks.com.ConfServiceFactory;
import com.genesyslab.platform.applicationblocks.com.IConfService;
import com.genesyslab.platform.commons.connection.ConnectionException;
import com.genesyslab.platform.commons.connection.configuration.PropertyConfiguration;
import com.genesyslab.platform.commons.connection.tls.KeyManagerHelper;
import com.genesyslab.platform.commons.connection.tls.SSLContextHelper;
import com.genesyslab.platform.commons.connection.tls.TrustManagerHelper;
import com.genesyslab.platform.commons.protocol.ChannelState;
import com.genesyslab.platform.commons.protocol.Endpoint;
import com.genesyslab.platform.commons.protocol.Protocol;
import com.genesyslab.platform.commons.protocol.ProtocolException;
import com.genesyslab.platform.configuration.protocol.ConfServerProtocol;
import com.genesyslab.platform.configuration.protocol.types.CfgAppType;
import java.io.IOException;
import java.net.URI;
import java.security.GeneralSecurityException;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

public final class GenesysServices {
  /**
   * Create a Genesys configuration service.
   *
   * @param username The user name
   * @param password The password
   * @param applicationName The application name
   * @param applicationType The application type
   * @param endpoint The endpoint
   * @return IConfService
   * @throws ConnectionException When a connection problem occurs
   */
  public static IConfService createConfigurationService(
      String username,
      String password,
      String applicationName,
      CfgAppType applicationType,
      Endpoint endpoint)
      throws ConnectionException {

    ConfServerProtocol protocol = new ConfServerProtocol(endpoint);
    protocol.setClientApplicationType(applicationType.ordinal());
    protocol.setUserName(username);
    protocol.setUserPassword(password);
    protocol.setClientName(applicationName);

    try {
      IConfService configurationService = ConfServiceFactory.createConfService(protocol);
      configurationService.getProtocol().open();
      return configurationService;
    } catch (Exception exception) {
      throw new ConnectionException("Can't create IConfService", exception);
    }
  }

  /**
   * Release a Genesys configuration service.
   *
   * @param service The service
   * @throws InterruptedException in case the close operation was interrupted.
   * @throws ProtocolException if there is any problem related to the connection close
   */
  public static void releaseConfigurationService(IConfService service)
      throws InterruptedException, ProtocolException {
    if (service != null) {
      Protocol protocol = service.getProtocol();

      if (protocol.getState() != ChannelState.Closed) {
        protocol.close();
      }

      ConfServiceFactory.releaseConfService(service);
    }
  }

  /**
   * Create an endpoint.
   *
   * @param primaryUri The primary URI
   * @param id The id
   * @param addpClientTimeout The client timeout
   * @param addpServerTimeout The server timeout
   * @param tlsEnabled Enable/disable TLS
   * @return Endpoint
   * @throws ConnectionException When a connection problem occurs
   */
  public static Endpoint createEndPoint(
      URI primaryUri,
      String id,
      Integer addpClientTimeout,
      Integer addpServerTimeout,
      boolean tlsEnabled)
      throws ConnectionException {
    PropertyConfiguration propertyConfiguration = new PropertyConfiguration();
    propertyConfiguration.setUseAddp(true);
    propertyConfiguration.setAddpClientTimeout(addpClientTimeout);
    propertyConfiguration.setAddpServerTimeout(addpServerTimeout);
    propertyConfiguration.setTLSEnabled(tlsEnabled);

    return new Endpoint(
        id,
        primaryUri.getHost(),
        primaryUri.getPort(),
        propertyConfiguration,
        tlsEnabled,
        createSslContext(),
        null);
  }

  private static SSLContext createSslContext() throws ConnectionException {
    try {
      KeyManager keyManager = KeyManagerHelper.createEmptyKeyManager();
      X509TrustManager trustManager = TrustManagerHelper.createDefaultTrustManager();
      return SSLContextHelper.createSSLContext(keyManager, trustManager);
    } catch (GeneralSecurityException | IOException exception) {
      throw new ConnectionException("Security configuration error.", exception);
    }
  }

  private GenesysServices() {}
}

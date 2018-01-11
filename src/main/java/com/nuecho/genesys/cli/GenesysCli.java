package com.nuecho.genesys.cli;

import static com.nuecho.genesys.cli.GenesysServices.createConfigurationService;
import static com.nuecho.genesys.cli.GenesysServices.createEndPoint;
import static java.lang.String.format;

import com.genesyslab.platform.applicationblocks.com.IConfService;
import com.genesyslab.platform.commons.connection.ConnectionException;
import com.genesyslab.platform.commons.protocol.Endpoint;
import com.genesyslab.platform.configuration.protocol.types.CfgAppType;
import com.nuecho.genesys.cli.compare.Compare;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import picocli.CommandLine;

@CommandLine.Command(
  name = "gen",
  description = "Genesys Command Line Interface",
  subcommands = {Compare.class}
)
public final class GenesysCli implements Runnable {
  private static final String DEFAULT_APPLICATION_NAME = "default";
  private static final String DEFAULT_SERVER_PORT = "2020";
  private static final int DEFAULT_SERVER_TIMEOUT = 20;
  private static final int DEFAULT_CLIENT_TIMEOUT = 10;

  @CommandLine.Option(
    names = {"-s", "--server"},
    paramLabel = "SERVER",
    description = "the Genesys server to connect to",
    required = true
  )
  private String server;

  @CommandLine.Option(
    names = {"-u", "--user"},
    paramLabel = "USER",
    description = "the Genesys username for the connection",
    required = true
  )
  private String username;

  @CommandLine.Option(
    names = {"-p", "--password"},
    paramLabel = "PASSWORD",
    description = "the Genesys user password for the connection",
    required = true
  )
  private String password;

  public static void main(String[] args) throws Exception {
    CommandLine.run(new GenesysCli(), System.err, args);
  }

  @Override
  public void run() {}

  /**
   * Connect to Genesys server.
   *
   * @return IConfService
   * @throws ConnectionException When a connection problem occurs
   * @throws URISyntaxException When URI is invalid
   */
  public IConfService connect() throws ConnectionException, URISyntaxException {
    String uri = format("tcp://%s:%s/", server, DEFAULT_SERVER_PORT);

    Endpoint endpoint =
        createEndPoint(
            new URI(uri),
            UUID.randomUUID().toString(),
            DEFAULT_CLIENT_TIMEOUT,
            DEFAULT_SERVER_TIMEOUT,
            false);
    return createConfigurationService(
        username, password, DEFAULT_APPLICATION_NAME, CfgAppType.CFGSCE, endpoint);
  }
}

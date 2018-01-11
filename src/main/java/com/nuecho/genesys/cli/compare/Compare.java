package com.nuecho.genesys.cli.compare;

import com.genesyslab.platform.applicationblocks.com.IConfService;
import com.genesyslab.platform.commons.connection.ConnectionException;
import com.nuecho.genesys.cli.GenesysCli;
import java.net.URISyntaxException;
import picocli.CommandLine;

@CommandLine.Command(
  name = "compare",
  description = "Compare Genesys configuration between two instances.",
  subcommands = {CompareAgents.class}
)
public final class Compare {
  @CommandLine.ParentCommand private GenesysCli genesysCli;

  public IConfService connect() throws ConnectionException, URISyntaxException {
    return genesysCli.connect();
  }
}

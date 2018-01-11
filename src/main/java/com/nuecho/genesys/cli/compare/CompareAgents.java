package com.nuecho.genesys.cli.compare;

import static com.google.common.collect.Lists.newArrayList;
import static com.nuecho.genesys.cli.GenesysServices.releaseConfigurationService;
import static java.util.stream.Collectors.toList;

import com.genesyslab.platform.applicationblocks.com.IConfService;
import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson;
import com.genesyslab.platform.applicationblocks.com.queries.CfgPersonQuery;
import com.genesyslab.platform.configuration.protocol.types.CfgFlag;
import difflib.DiffUtils;
import difflib.Patch;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import picocli.CommandLine;

@CommandLine.Command(
  name = "agents",
  description = "Compare Genesys agent configuration between two instances."
)
public final class CompareAgents implements Callable<Void> {
  @CommandLine.ParentCommand private Compare compare;

  @Override
  public Void call() throws Exception {
    IConfService service = null;
    try {
      service = compare.connect();

      CfgPersonQuery query = new CfgPersonQuery();
      query.setIsAgent(CfgFlag.CFGTrue.ordinal());
      Collection<CfgPerson> persons = service.retrieveMultipleObjects(CfgPerson.class, query);
      List<String> left = persons.stream().map(person -> person.getUserName()).collect(toList());
      List<String> right = newArrayList(left);

      /* Sample test, to use with 192.168.129.85 server
      right.remove(10);
      right.remove(10);
      right.remove(10);

      right.remove(50);
      right.remove(50);
      right.remove(50);

      right.set(5, "testerpatate");
      */

      Patch diff = DiffUtils.diff(left, right);
      System.out.println(diff.getDeltas());
    } finally {
      releaseConfigurationService(service);
    }

    return null;
  }
}

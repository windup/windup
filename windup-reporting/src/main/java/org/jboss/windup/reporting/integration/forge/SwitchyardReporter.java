package org.jboss.windup.reporting.integration.forge;

import java.io.File;

import org.jboss.windup.metadata.type.archive.ArchiveMetadata;
import org.jboss.windup.reporting.Reporter;

public class SwitchyardReporter implements Reporter
{
   @Override
   public void process(ArchiveMetadata archive, File reportDirectory)
   {
	   SwitchyardForgeController sfc = new SwitchyardForgeController();
	   sfc.process(archive, reportDirectory);
   }

}

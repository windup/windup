package org.jboss.windup.reporting.integration.forge;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.repositories.AddonRepositoryMode;
import org.jboss.forge.furnace.se.FurnaceFactory;
import org.jboss.forge.furnace.util.OperatingSystemUtils;
import org.jboss.windup.metadata.type.archive.ArchiveMetadata;
import org.jboss.windup.reporting.Reporter;

public class SwitchyardReporter implements Reporter
{

   @Override
   public void process(ArchiveMetadata archive, File reportDirectory)
   {
      File forgeOutput = new File(reportDirectory, "forge");
      try
      {
         FileUtils.forceMkdir(forgeOutput);

         final Furnace furnace;

         furnace = FurnaceFactory.getInstance();
         furnace.addRepository(AddonRepositoryMode.MUTABLE, new File(OperatingSystemUtils.getUserHomeDir(), ".windup"));
         furnace.startAsync();

         while (!furnace.getStatus().isStarted())
         {
            System.out.println("FURNACE STATUS: " + furnace.getStatus());
            Thread.sleep(100);
         }

         furnace.stop();
         System.out.println("Furnace stopped.");

      }
      catch (IOException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      catch (InterruptedException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

}

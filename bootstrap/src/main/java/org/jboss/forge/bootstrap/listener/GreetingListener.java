/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.bootstrap.listener;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.exception.ContainerException;
import org.jboss.forge.furnace.spi.ContainerLifecycleListener;

public class GreetingListener implements ContainerLifecycleListener
{
   private final Logger logger = Logger.getLogger(getClass().getName());

   @Override
   public void beforeStart(Furnace furnace) throws ContainerException
   {
      if (furnace.isServerMode())
      {
         StringWriter sw = new StringWriter();
         PrintWriter out = new PrintWriter(sw, true);
         out.println();
         out.println(" _       ___           __          ");
         out.println("| |     / (_)___  ____/ /_  ______ ");
         out.println("| | /| / / / __ \\/ __  / / / / __ \\");
         out.println("| |/ |/ / / / / / /_/ / /_/ / /_/ /");
         out.println("|__/|__/_/_/ /_/\\__,_/\\__,_/ .___/ ");
         out.println("                          /_/      ");
         out.println("");
         out.print("JBoss Forge, version [ ");
         out.print(furnace.getVersion());
         out.print(" ] - JBoss, by Red Hat, Inc. [ http://forge.jboss.org ]");
         out.println();
         logger.info(sw.toString());
         System.out.println(sw.toString());
      }
   }

   @Override
   public void beforeStop(Furnace forge) throws ContainerException
   {
      // Do nothing
   }

   @Override
   public void afterStop(Furnace forge) throws ContainerException
   {
      // Do nothing
   }

   @Override
   public void beforeConfigurationScan(Furnace forge) throws ContainerException
   {
      // Do nothing
   }

   @Override
   public void afterConfigurationScan(Furnace forge) throws ContainerException
   {
      // Do nothing
   }

}

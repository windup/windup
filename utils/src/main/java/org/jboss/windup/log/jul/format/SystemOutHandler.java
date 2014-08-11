package org.jboss.windup.log.jul.format;

import java.util.logging.ConsoleHandler;

/**
 * ConsoleHandler dumps everything to System.err; this changes that to System.out.
 * 
 * @author Ondrej Zizka
 */
public class SystemOutHandler extends ConsoleHandler {

  public SystemOutHandler() {
    //sealed = false;
    super();
    setOutputStream(System.out);
    //sealed = true;
  }
}

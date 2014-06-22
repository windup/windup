package org.jboss.windup.log;

import java.util.logging.ConsoleHandler;

/**
 *
 * @author j
 */
public class SystemOutHandler extends ConsoleHandler {

  public SystemOutHandler() {
    //sealed = false;
    super();
    setOutputStream(System.out);
    //sealed = true;
  }
}

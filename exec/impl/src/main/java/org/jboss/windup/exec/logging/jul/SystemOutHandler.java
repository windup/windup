package org.jboss.windup.exec.logging.jul;

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

package org.jboss.windup.tooling.quickfix;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface QuickfixService extends Remote {
    String LOOKUP_NAME = "QuickfixService";

    String transform(String transformationID, QuickfixLocationDTO locationDTO) throws RemoteException;
}

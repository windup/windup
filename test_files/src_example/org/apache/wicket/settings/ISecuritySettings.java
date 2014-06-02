package org.apache.wicket.settings;

import org.apache.wicket.authentication.*;
import org.apache.wicket.util.crypt.*;
import org.apache.wicket.authorization.*;

public interface ISecuritySettings{
    public static final String DEFAULT_ENCRYPTION_KEY="WiCkEt-FRAMEwork";
    IAuthorizationStrategy getAuthorizationStrategy();
    IAuthenticationStrategy getAuthenticationStrategy();
    ICryptFactory getCryptFactory();
    boolean getEnforceMounts();
    IUnauthorizedComponentInstantiationListener getUnauthorizedComponentInstantiationListener();
    void setAuthorizationStrategy(IAuthorizationStrategy p0);
    void setAuthenticationStrategy(IAuthenticationStrategy p0);
    void setCryptFactory(ICryptFactory p0);
    void setEnforceMounts(boolean p0);
    void setUnauthorizedComponentInstantiationListener(IUnauthorizedComponentInstantiationListener p0);
}

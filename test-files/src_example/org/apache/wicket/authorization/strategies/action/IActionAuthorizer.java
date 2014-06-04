package org.apache.wicket.authorization.strategies.action;

import org.apache.wicket.authorization.*;
import org.apache.wicket.*;

public interface IActionAuthorizer extends IClusterable{
    Action getAction();
    boolean authorizeAction(Component p0);
}

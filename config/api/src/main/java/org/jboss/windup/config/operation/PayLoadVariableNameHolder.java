package org.jboss.windup.config.operation;

/**
 * Instance carrying the name of the payload variable name. Used to set by the name by the currently processed {@link Iteration}.
 */
public interface PayLoadVariableNameHolder
{

    public boolean hasVariableNameSet();
    public void setVariableName(String variable);
}

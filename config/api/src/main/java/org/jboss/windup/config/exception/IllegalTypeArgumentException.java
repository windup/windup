package org.jboss.windup.config.exception;

public class IllegalTypeArgumentException extends IllegalArgumentException
{
    private static final long serialVersionUID = 1L;

    private String variableName;
    private Class<?> expectedInterface;
    private Class<?> actualType;

    public IllegalTypeArgumentException(String variableName, Class<?> expectedInterface, Class<?> actualType)
    {
        this.variableName = variableName;
        this.expectedInterface = expectedInterface;
        this.actualType = actualType;
    }

    @Override
    public String getMessage()
    {
        StringBuilder implementedInterfaces = new StringBuilder();
        for (Class<?> iface : actualType.getInterfaces())
        {
            if (implementedInterfaces.length() != 0)
            {
                implementedInterfaces.append(", ");
            }
            implementedInterfaces.append(iface.getName());
        }
        String message = "Variable \"" + variableName + "\" does not implement expected interface \""
                    + expectedInterface.getCanonicalName() + "\", actual implemented interfaces are: "
                    + implementedInterfaces.toString();
        return message;
    }
}

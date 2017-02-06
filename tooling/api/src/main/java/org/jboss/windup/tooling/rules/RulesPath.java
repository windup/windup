package org.jboss.windup.tooling.rules;

import java.io.Serializable;

public interface RulesPath extends Serializable
{
    int getVersion();

    void setVersion(int version);

    /**
     * Contains the path to the rules directory.
     */
    String getPath();

    /**
     * Contains the path to the rules directory.
     */
    void setPath(String inputPath);

    /**
     * Contains the type of rules path (for example, system provided vs user provided).
     */
    RulesPathType getRulesPathType();

    /**
     * Contains the type of rules path (for example, system provided vs user provided).
     */
    void setRulesPathType(RulesPathType rulesPathType);

    /**
     * Contains a load error if there were any issues loading rules from this path.
     */
    String getLoadError();

    /**
     * Contains a load error if there were any issues loading rules from this path.
     */
    void setLoadError(String loadError);

    public enum RulesPathType
    {
        SYSTEM_PROVIDED, USER_PROVIDED
    }
}

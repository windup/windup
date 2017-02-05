package org.jboss.windup.tooling.rules;

public class RulesPathImpl implements RulesPath 
{
	private static final long serialVersionUID = 1L;

    private int version;

    private String path;

    private String loadError;

    private RulesPathType rulesPathType;

    public RulesPathImpl(String path, RulesPathType rulesPathType)
    {
        this.path = path;
        this.rulesPathType = rulesPathType;
    }

    public int getVersion()
    {
        return version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }

    /**
     * Contains the path to the rules directory.
     */
    public String getPath()
    {
        return path;
    }

    /**
     * Contains the path to the rules directory.
     */
    public void setPath(String path)
    {
        this.path = path;
    }

    /**
     * Contains the type of rules path (for example, system provided vs user provided).
     */
    public RulesPathType getRulesPathType()
    {
        return rulesPathType;
    }

    /**
     * Contains the type of rules path (for example, system provided vs user provided).
     */
    public void setRulesPathType(RulesPathType rulesPathType)
    {
        this.rulesPathType = rulesPathType;
    }

    /**
     * Contains a load error if there were any issues loading rules from this path.
     */
    public String getLoadError()
    {
        return loadError;
    }

    /**
     * Contains a load error if there were any issues loading rules from this path.
     */
    public void setLoadError(String loadError)
    {
        this.loadError = loadError;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (!(o instanceof RulesPathImpl))
            return false;

        RulesPathImpl rulesPath = (RulesPathImpl) o;

        if (path != null ? !path.equals(rulesPath.path) : rulesPath.path != null)
            return false;
        return rulesPathType == rulesPath.rulesPathType;
    }

    @Override
    public int hashCode()
    {
        int result = path != null ? path.hashCode() : 0;
        result = 31 * result + (rulesPathType != null ? rulesPathType.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "RulesPath{" +
                    ", version=" + version +
                    ", path='" + path + '\'' +
                    ", rulesPathType=" + rulesPathType +
                    '}';
    }
}

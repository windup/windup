package org.jboss.windup.rules.apps.xml.condition;

import java.util.HashMap;
import java.util.Map;

public class XmlFileParameterMatchCache
{
    private Map<Integer, Map<String, String>> vars = new HashMap<>();

    public void addFrame(int frameID)
    {
        if (vars.containsKey(frameID))
        {
            vars.get(frameID).clear();
        }
        else
        {
            vars.put(frameID, new HashMap<String, String>());
        }
    }

    public boolean checkVariable(int frameID, String key, String value)
    {
        for (int i = frameID; i >= 0; i--)
        {
            String existingValue = vars.get(i).get(key);
            if (existingValue != null && !existingValue.equals(value))
            {
                return false;
            }
        }
        return true;
    }

    public void addVariable(int frameID, String key, String value)
    {
        vars.get(frameID).put(key, value);
    }

    public Map<String, String> getVariables(int frameID)
    {
        Map<String, String> result = new HashMap<>();
        for (int i = 0; i < vars.size(); i++)
        {
            Map<String, String> existingVars = vars.get(i);
            for (Map.Entry<String, String> existingVar : existingVars.entrySet())
            {
                result.put(existingVar.getKey(), existingVar.getValue());
            }
        }
        return result;
    }
}

package org.jboss.windup.qs.identarch.util;

import org.jboss.windup.qs.identarch.IdentifyArchivesLoadConfigRules;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class ResourceUtils {

    public static String getResourcesPath(Class<IdentifyArchivesLoadConfigRules> cls)
    {
        if (cls.getPackage() == null)
            return "/";

        return "/" + cls.getPackage().getName().replace('.', '/');
    }

}// class

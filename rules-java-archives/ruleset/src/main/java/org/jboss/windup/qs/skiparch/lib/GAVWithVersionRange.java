package org.jboss.windup.qs.skiparch.lib;

import org.jboss.windup.qs.identarch.model.GAV;
import org.apache.commons.lang.StringUtils;

/**
 * Bears information about a skipped artifact; Wildcards are possible.
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class GAVWithVersionRange extends GAV {

    // The 2nd version of version range.
    private final String versionMax;



    public GAVWithVersionRange(String sha1, String groupId, String artifactId, String version, String versionRangeMax)
    {
        super(null, groupId, artifactId, version);
        this.versionMax = versionRangeMax;
    }


    /**
     * Parses the information from "G:A:V[:C]".
     */
    static GAVWithVersionRange fromGAVV(String gavvStr)
    {
        String[] parts = StringUtils.split(gavvStr, " :");
        if(parts.length < 3)
            throw new IllegalArgumentException("Expected GAV definition format is 'G:A:V[~V][:C]', was: " + gavvStr);

        String ver2 = null;
        String[] versions = StringUtils.split(parts[2], '~');
        if(versions.length > 1){
            ver2 = versions[1];
        }

        GAVWithVersionRange gavv = new GAVWithVersionRange(null, parts[0], parts[1], parts[2], ver2);

        if(parts.length >= 4)
            gavv.setClassifier(parts[3]);

        return gavv;
    }



    public String getVersionMax()
    {
        return versionMax;
    }

}// class

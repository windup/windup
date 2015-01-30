package org.jboss.windup.qs.identarch.model;

import com.tinkerpop.blueprints.Vertex;
import info.aduna.lang.ObjectUtil;
import java.util.regex.Pattern;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import static org.jboss.windup.qs.identarch.lib.HashToGAVIdentifier.SHA1_LENGTH;

/**
 * Maven G:A:V coordinates.
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class GAV implements GAVModel
{
    @Override
    public String toString()
    {
        return "GAV{" + "sha1:" + sha1 + " " + groupId + ":" + artifactId + ":" + version + ":" + classifier + '}';
    }

    private String sha1;
    private String groupId;
    private String artifactId;
    private String version;
    private String classifier;


    /**
     * Parses the information from "G:A:V[:C]".
     */
    public static GAV fromGAV(String gavStr)
    {
        String[] parts = StringUtils.split(gavStr, " :");
        if(parts.length < 3)
            throw new IllegalArgumentException("Expected GAV definition format is 'G:A:V[:C]', was: " + gavStr);
        GAV gav = new GAV(parts[0], parts[1], parts[2]);
        if(parts.length >= 4)
            gav.setClassifier(parts[3]);

        return gav;
    }

    /**
     * Parses the information from "SHA1 G:A:V[:C]".
     */
    public static GAV fromSHA1AndGAV(String sha1AndGAV)
    {
        String[] parts = StringUtils.split(sha1AndGAV, " :");
        if(parts.length < 4)
            throw new IllegalArgumentException("Expected GAV definition format is 'SHA1 G:A:V[:C]', was: " + sha1AndGAV);
        GAV gav = new GAV(parts[0], parts[1], parts[2], parts[3]);
        if(parts.length >= 5)
            gav.setClassifier(parts[4]);

        return gav;
    }


    static final Pattern PATTERN_SHA1AndGAV = Pattern.compile(
                    "\\p{XDigit}{0," + SHA1_LENGTH + "}"
                    + " [\\w-]+(\\.[\\w-]+)+"
                    + ":[\\w-]+"
                    + ":[\\w-\\.]+"
                    + "(:[\\w-\\.])?");

    public static boolean isValidSHA1AndGAV(String line)
    {
        return PATTERN_SHA1AndGAV.matcher(line).matches();
    }



    public GAV(String sha1, String groupId, String artifactId, String version)
    {
        this.sha1 = sha1;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }


    public GAV(String groupId, String artifactId, String version)
    {
        this(null, groupId, artifactId, version);
    }



    //<editor-fold defaultstate="collapsed" desc="get/set">

    @Override
    public String getSha1()
    {
        return sha1;
    }


    @Override
    public String getGroupId()
    {
        return groupId;
    }


    @Override
    public String getArtifactId()
    {
        return artifactId;
    }


    @Override
    public String getVersion()
    {
        return version;
    }


    @Override
    public String getClassifier()
    {
        return classifier;
    }

    @Override
    public GAV setSha1(String sha1)
    {
        this.sha1 = sha1;
        return this;
    }


    @Override
    public GAV setGroupId(String groupId)
    {
        this.groupId = groupId;
        return this;
    }


    @Override
    public GAV setArtifactId(String artifactId)
    {
        this.artifactId = artifactId;
        return this;
    }


    @Override
    public GAV setVersion(String version)
    {
        this.version = version;
        return this;
    }


    @Override
    public GAV setClassifier(String classifier)
    {
        this.classifier = classifier;
        return this;
    }
    //</editor-fold>


    @Override
    public boolean equals(Object other)
    {
        if (!(other instanceof GAVModel))
            return false;
        GAVModel gav = (GAVModel) other;
        if (!StringUtils.equals(this.groupId, gav.getGroupId()))
                return false;
        if (!StringUtils.equals(this.artifactId, gav.getArtifactId()))
                return false;
        if (!StringUtils.equals(this.version, gav.getVersion()))
                return false;
        if (this.sha1 != null && gav.getSha1() != null && !(this.sha1.equals(gav.getSha1())))
            return false;
        
        return true;
    }



    @Override
    public String toPrettyString()
    {
        return toString();
    }

    @Override
    public Vertex asVertex()
    {
        return null;
    }

}// class

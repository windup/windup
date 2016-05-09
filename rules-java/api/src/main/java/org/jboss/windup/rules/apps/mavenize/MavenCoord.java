package org.jboss.windup.rules.apps.mavenize;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.jboss.windup.rules.apps.java.archives.model.ArchiveCoordinateModel;

/**
 * A POJO for Maven coordinate. A counterpart for ArchiveCoordinateModel.
 */
public class MavenCoord
{
    private String groupId;
    private String artifactId;
    private String version; // May be null if defined in a BOM.
    private String classifier;
    private String packaging;

    // This should not really be here, but for the simplicity, I keep it here.
    private String scope;
    private String comment;
    private Set<MavenCoord> exclusions = new HashSet<>();


    MavenCoord()
    {
    }


    @Deprecated
    MavenCoord(ArchiveCoordinateModel coordinate)
    {
        this.groupId = coordinate.getGroupId();
        this.artifactId = coordinate.getArtifactId();
        this.version = coordinate.getVersion();
        this.classifier = coordinate.getClassifier();
        this.packaging = coordinate.getPackaging();
    }

    static MavenCoord from(ArchiveCoordinateModel coordinate)
    {
        return new MavenCoord()
        .setGroupId(coordinate.getGroupId())
        .setArtifactId(coordinate.getArtifactId())
        .setVersion(coordinate.getVersion())
        .setClassifier(coordinate.getClassifier())
        .setPackaging(coordinate.getPackaging());
    }

    public static final Pattern REGEX_GAVCP = Pattern.compile("([^: ]+):([^: ]+):([^: ]+)(:[^: ]+)?(:[^: ]+)?");

    public static MavenCoord fromGAVPC(String coordGavpc)
    {
        Matcher mat = REGEX_GAVCP.matcher(coordGavpc);
        if (!mat.matches())
            throw new IllegalArgumentException("Wrong Maven coordinates format, must be G:A:V[:P[:C]] . " + coordGavpc);

        return new MavenCoord()
        .setGroupId(mat.group(1))
        .setArtifactId(mat.group(2))
        .setVersion(mat.group(3))
        .setPackaging(mat.group(4))
        .setClassifier(mat.group(5));
    }

    MavenCoord(String groupId, String artifactId, String version)
    {
        this(groupId, artifactId, version, "pom");
    }


    MavenCoord(String groupId, String artifactId, String version, String packaging)
    {
        this(groupId, artifactId, version, null, packaging);
    }


    MavenCoord(String groupId, String artifactId, String version, String classifier, String packaging)
    {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.classifier = classifier;
        this.packaging = packaging;
    }




    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 79 * hash + Objects.hashCode(this.getGroupId());
        hash = 79 * hash + Objects.hashCode(this.getArtifactId());
        hash = 79 * hash + Objects.hashCode(this.getVersion());
        hash = 79 * hash + Objects.hashCode(this.getClassifier());
        hash = 79 * hash + Objects.hashCode(this.getPackaging());
        return hash;
    }


    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final MavenCoord other = (MavenCoord) obj;
        if (!Objects.equals(this.groupId, other.groupId))
            return false;
        if (!Objects.equals(this.artifactId, other.artifactId))
            return false;
        if (!Objects.equals(this.version, other.version))
            return false;
        if (!Objects.equals(this.classifier, other.classifier))
            return false;
        if (!Objects.equals(this.packaging, other.packaging))
            return false;
        return true;
    }


    public String getGroupId()
    {
        return groupId;
    }


    public MavenCoord setGroupId(String groupId)
    {
        this.groupId = groupId;
        return this;
    }


    public String getArtifactId()
    {
        return artifactId;
    }


    public MavenCoord setArtifactId(String artifactId)
    {
        this.artifactId = artifactId;
        return this;
    }


    public String getVersion()
    {
        return version;
    }


    public MavenCoord setVersion(String version)
    {
        this.version = version;
        return this;
    }


    public String getClassifier()
    {
        return classifier;
    }


    public MavenCoord setClassifier(String classifier)
    {
        this.classifier = classifier;
        return this;
    }


    public String getPackaging()
    {
        return packaging;
    }


    public MavenCoord setPackaging(String packaging)
    {
        this.packaging = packaging;
        return this;
    }


    public String getScope()
    {
        return scope;
    }


    public MavenCoord setScope(String scope)
    {
        this.scope = scope;
        return this;
    }


    public String getComment()
    {
        return comment;
    }


    public MavenCoord setComment(String comment)
    {
        this.comment = comment;
        return this;
    }


    public Set<MavenCoord> getExclusions()
    {
        return exclusions;
    }

    public MavenCoord addExclusion(MavenCoord coord){
        this.getExclusions().add(coord);
        return this;
    }


    @Override
    public String toString()
    {
        return '{' + groupId + ":" + artifactId + ":" + version + ":" + StringUtils.defaultString(classifier) + ":" + packaging + ",s:" + scope +'}';
    }
}

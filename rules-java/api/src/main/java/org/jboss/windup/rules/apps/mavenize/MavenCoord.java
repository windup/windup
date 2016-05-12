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
 *
 * @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, zizka@seznam.cz</a>
 */
public class MavenCoord
{
    private String groupId;
    private String artifactId;
    private String version; // May be null if defined in a BOM.
    private String classifier;
    private String packaging;

    // This should rather be in some kind of DependencyDeclaration, but for the simplicity, let's keep it here.
    private String scope;
    private String comment;
    private Set<MavenCoord> exclusions = new HashSet<>();


    /**
     * Creates an empty coordinate.
     */
    MavenCoord()
    {
    }

    /**
     * Creates a coordinate from the given {@link ArchiveCoordinateModel}.
     */
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

    /**
     * Creates a {@link MavenCoord} from the given coordinate String.
     */
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


    /**
     * Contains the group id.
     */
    public String getGroupId()
    {
        return groupId;
    }

    /**
     * Contains the group id.
     */
    public MavenCoord setGroupId(String groupId)
    {
        this.groupId = groupId;
        return this;
    }

    /**
     * Contains the Artifact id.
     */
    public String getArtifactId()
    {
        return artifactId;
    }

    /**
     * Contains the Artifact id.
     */
    public MavenCoord setArtifactId(String artifactId)
    {
        this.artifactId = artifactId;
        return this;
    }

    /**
     * Contains the version.
     */
    public String getVersion()
    {
        return version;
    }

    /**
     * Contains the version.
     */
    public MavenCoord setVersion(String version)
    {
        this.version = version;
        return this;
    }

    /**
     * Contains the Classifier.
     */
    public String getClassifier()
    {
        return classifier;
    }

    /**
     * Contains the Classifier.
     */
    public MavenCoord setClassifier(String classifier)
    {
        this.classifier = classifier;
        return this;
    }

    /**
     * Contains the Packaging setting (eg, 'jar' or 'ear').
     */
    public String getPackaging()
    {
        return packaging;
    }

    /**
     * Contains the Packaging setting (eg, 'jar' or 'ear').
     */
    public MavenCoord setPackaging(String packaging)
    {
        this.packaging = packaging;
        return this;
    }

    /**
     * Contains the scope (eg, 'compile').
     */
    public String getScope()
    {
        return scope;
    }

    /**
     * Contains the scope (eg, 'compile').
     */
    public MavenCoord setScope(String scope)
    {
        this.scope = scope;
        return this;
    }


    /**
     * Contains a textual comment.
     */
    public String getComment()
    {
        return comment;
    }

    /**
     * Contains a textual comment.
     */
    public MavenCoord setComment(String comment)
    {
        this.comment = comment;
        return this;
    }

    /**
     * Contains any exclusions from this dependency.
     */
    public Set<MavenCoord> getExclusions()
    {
        return exclusions;
    }

    /**
     * Contains any exclusions from this dependency.
     */
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

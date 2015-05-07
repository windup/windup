package org.jboss.windup.graph.model;

import org.jboss.windup.graph.model.resource.FileModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.graph.model.resource.DirectoryModel;
import org.jboss.windup.graph.model.resource.PathModel;

/**
 * Contains the global configuration for the current Windup execution.
 */
@TypeValue(WindupConfigurationModel.TYPE)
public interface WindupConfigurationModel extends WindupVertexFrame
{
    public static final String INPUT_PATH = "inputPath";

    public static final String TYPE = "BaseWindupConfiguration";

    public static final String USER_RULES_PATH = "userRulesPath";
    public static final String USER_IGNORE_PATH = "userIgnorePath";
    public static final String OFFLINE_MODE = "fetchRemoteResources";

    /**
     * The input path to scan
     */
    @Adjacency(label = INPUT_PATH, direction = Direction.OUT)
    PathModel getInputPath();

    /**
     * The input path to scan
     */
    @Adjacency(label = INPUT_PATH, direction = Direction.OUT)
    void setInputPath(PathModel inputPath);

    /**
     * The location for user provided rules directories (typically Groovy or XML Rules)
     */
    @Adjacency(label = USER_RULES_PATH, direction = Direction.OUT)
    void addUserRulesPath(PathModel userRulesPath);

    /**
     * The location for user provided ignore directory (list of ignored jar files)
     */
    @Adjacency(label = USER_IGNORE_PATH, direction = Direction.OUT)
    void addUserIgnorePath(PathModel userIgnorePath);

    /**
     * The location for user provided rules directories (typically Groovy or XML Rules)
     */
    @Adjacency(label = USER_RULES_PATH, direction = Direction.OUT)
    Iterable<PathModel> getUserRulesPaths();

    /**
     * The location for user provided rules directories (typically Groovy or XML Rules)
     */
    @Adjacency(label = USER_IGNORE_PATH, direction = Direction.OUT)
    Iterable<PathModel> getUserIgnorePaths();

    /**
     * Where to put the report and other files produced during Windup execution.
     */
    @Adjacency(label = "outputPath", direction = Direction.OUT)
    DirectoryModel getOutputPath();

    /**
     * Where to put the report and other files produced during Windup execution.
     */
    @Adjacency(label = "outputPath", direction = Direction.OUT)
    void setOutputPath(DirectoryModel outputPath);

    /**
     * Indicates whether or not to function in offline mode
     */
    @Property(OFFLINE_MODE)
    boolean isOfflineMode();

    @Property(OFFLINE_MODE)
    void setOfflineMode(boolean offline);
}

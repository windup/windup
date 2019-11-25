package org.jboss.windup.graph.model;

import org.jboss.windup.graph.model.resource.FileModel;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.Property;

import java.util.List;

/**
 * Contains the global configuration for the current Windup execution.
 */
@TypeValue(WindupConfigurationModel.TYPE)
public interface WindupConfigurationModel extends WindupVertexFrame
{
    String TYPE = "WindupConfigurationModel";

    String INPUT_PATH = "inputPath";
    String USER_RULES_PATH = "userRulesPath";
    String USER_LABELS_PATH = "userLabelsPath";
    String USER_IGNORE_PATH = "userIgnorePath";
    String ONLINE_MODE = "fetchRemoteResources";
    String OUTPUT_PATH = "outputPath";
    String SOURCE_TECHNOLOGY = "sourceTechnology";
    String TARGET_TECHNOLOGY = "targetTechnology";
    String CSV_MODE = "csv";
    String KEEP_WORKING_DIRECTORIES = "keepWorkDirs";
    String SKIP_REPORTS_RENDERING = "skipReports";

    /**
     * The input path to scan
     */
    @Adjacency(label = INPUT_PATH, direction = Direction.OUT)
    List<FileModel> getInputPaths();

    /**
     * The input path to scan
     */
    @Adjacency(label = INPUT_PATH, direction = Direction.OUT)
    void setInputPaths(Iterable<FileModel> inputPaths);

    /**
     * The input path to scan
     */
    @Adjacency(label = INPUT_PATH, direction = Direction.OUT)
    void addInputPath(FileModel inputPath);

    /**
     * The location for user provided rules directories (typically Groovy or XML Rules)
     */
    @Adjacency(label = USER_RULES_PATH, direction = Direction.OUT)
    void addUserRulesPath(FileModel userRulesPath);

    /**
     * The location for user provided labels directories (typically XML Labels)
     */
    @Adjacency(label = USER_LABELS_PATH, direction = Direction.OUT)
    void addUserLabelsPath(FileModel userLabelsPath);

    /**
     * The location for user provided ignore directory (list of ignored jar files)
     */
    @Adjacency(label = USER_IGNORE_PATH, direction = Direction.OUT)
    void addUserIgnorePath(FileModel userIgnorePath);

    /**
     * The location for user provided rules directories (typically Groovy or XML Rules)
     */
    @Adjacency(label = USER_RULES_PATH, direction = Direction.OUT)
    List<FileModel> getUserRulesPaths();

    /**
     * The location for user provided labels directories (typically XML Labels)
     */
    @Adjacency(label = USER_LABELS_PATH, direction = Direction.OUT)
    List<FileModel> getUserLabelsPaths();

    /**
     * The location for user provided rules directories (typically Groovy or XML Rules)
     */
    @Adjacency(label = USER_IGNORE_PATH, direction = Direction.OUT)
    List<FileModel> getUserIgnorePaths();

    /**
     * Where to put the report and other files produced during Windup execution.
     */
    @Adjacency(label = OUTPUT_PATH, direction = Direction.OUT)
    FileModel getOutputPath();

    /**
     * Where to put the report and other files produced during Windup execution.
     */
    @Adjacency(label = OUTPUT_PATH, direction = Direction.OUT)
    void setOutputPath(FileModel outputPath);

    /**
     * Indicates whether or not to function in online mode (network access allowed)
     */
    @Property(ONLINE_MODE)
    boolean isOnlineMode();

    /**
     * Indicates whether or not to function in online mode (network access allowed)
     */
    @Property(ONLINE_MODE)
    void setOnlineMode(boolean onlineMode);

    /**
     * Contains the id of the source technology (the technology being migrated from).
     */
    @Adjacency(label = SOURCE_TECHNOLOGY, direction = Direction.OUT)
    List<TechnologyReferenceModel> getSourceTechnologies();

    /**
     * Contains the id of the source technology (the technology being migrated from).
     */
    @Adjacency(label = SOURCE_TECHNOLOGY, direction = Direction.OUT)
    void addSourceTechnology(TechnologyReferenceModel technology);

    /**
     * Contains the id of the target technology (the technology being migrated to).
     */
    @Adjacency(label = TARGET_TECHNOLOGY, direction = Direction.OUT)
    List<TechnologyReferenceModel> getTargetTechnologies();

    /**
     * Contains the id of the target technology (the technology being migrated to).
     */
    @Adjacency(label = TARGET_TECHNOLOGY, direction = Direction.OUT)
    void addTargetTechnology(TechnologyReferenceModel technology);

    /**
     * Indicates whether or not to export CSV file
     */
    @Property(CSV_MODE)
    boolean isExportingCSV();

    /**
     * Indicates whether or not to export CSV file
     */
    @Property(CSV_MODE)
    void setExportingCSV(boolean csv);

    /**
     * Indicates whether or not to keep working directories (graph and unzipped archive data)
     */
    @Property(KEEP_WORKING_DIRECTORIES)
    Boolean isKeepWorkDirectories();

    /**
     * Indicates whether or not to keep working directories (graph and unzipped archive data)
     */
    @Property(KEEP_WORKING_DIRECTORIES)
    void setKeepWorkDirectories(Boolean keep);

    /**
     * Indicate whether skip all reports rendering
     *
     * @return
     */
    @Property(SKIP_REPORTS_RENDERING)
    Boolean isSkipReportsRendering();

    /**
     * Indicate whether skip all reports rendering
     *
     */
    @Property(SKIP_REPORTS_RENDERING)
    void setSkipReportsRendering(Boolean skip);
}
